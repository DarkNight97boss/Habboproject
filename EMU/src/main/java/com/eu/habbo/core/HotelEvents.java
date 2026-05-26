package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Random hotel-wide events + time-based XP multiplier.
 *
 * Every {@code hotel.events.interval.minutes} the scheduler picks one of the
 * event types at random and applies it to every online user:
 *   FREE_CREDITS   — drops N credits in everyone's wallet (and broadcasts).
 *   FREE_PIXELS    — same with duckets.
 *   DOUBLE_XP      — boosts battle-pass XP by a factor for the next minutes.
 *   BROADCAST_TIP  — flavour-only broadcast (no rewards), for traffic.
 *
 * In addition, this class is the single source of truth for the *current* XP
 * multiplier the rest of the code should apply ({@link #currentXpMultiplier()}):
 * a base time-of-week multiplier (weekend +30%, night +50%) gets multiplied
 * by any active event-driven boost.
 *
 * Config keys (emulator_settings):
 *   hotel.events.enabled               (default 1)
 *   hotel.events.interval.minutes      (default 30)
 *   hotel.events.initial.delay.seconds (default 120)
 *   hotel.events.free_credits.amount   (default 50)
 *   hotel.events.free_pixels.amount    (default 100)
 *   hotel.events.double_xp.factor      (default 2.0 — float)
 *   hotel.events.double_xp.minutes     (default 60)
 *   xp.multiplier.weekend              (default 1.3)
 *   xp.multiplier.night                (default 1.5  — 22:00-08:00 server time)
 */
public final class HotelEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotelEvents.class);

    private static final AtomicLong eventXpMultiplierExpiresAt = new AtomicLong(0L);
    private static final java.util.concurrent.atomic.AtomicReference<Double> eventXpMultiplier =
            new java.util.concurrent.atomic.AtomicReference<>(1.0);

    private ScheduledExecutorService scheduler;

    public HotelEvents() {
        if (!isEnabled()) {
            LOGGER.info("HotelEvents -> disabled");
            return;
        }
        int initialDelay = Emulator.getConfig().getInt("hotel.events.initial.delay.seconds", 120);
        int intervalMinutes = Math.max(1, Emulator.getConfig().getInt("hotel.events.interval.minutes", 30));

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HotelEvents");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(this::tick, initialDelay, intervalMinutes * 60L, TimeUnit.SECONDS);
        LOGGER.info("HotelEvents -> Loaded! (tick every {} min)", intervalMinutes);
    }

    public static boolean isEnabled() {
        return Emulator.getConfig().getBoolean("hotel.events.enabled", true);
    }

    /** Combined XP multiplier (time-of-week × current event boost). */
    public static double currentXpMultiplier() {
        double base = 1.0;
        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            DayOfWeek d = now.getDayOfWeek();
            if (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY) {
                base *= Emulator.getConfig().getDouble("xp.multiplier.weekend", 1.3);
            }
            int hour = now.getHour();
            if (hour >= 22 || hour < 8) {
                base *= Emulator.getConfig().getDouble("xp.multiplier.night", 1.5);
            }
        } catch (Exception ignored) {
        }
        double event = eventXpMultiplierExpiresAt.get() > Emulator.getIntUnixTimestamp()
                ? eventXpMultiplier.get() : 1.0;
        return base * event;
    }

    /** Apply the multiplier to an XP value (rounded to int). Min 0. */
    public static int applyXpMultiplier(int baseXp) {
        if (baseXp <= 0) return baseXp;
        return Math.max(0, (int) Math.round(baseXp * currentXpMultiplier()));
    }

    private enum EventKind { FREE_CREDITS, FREE_PIXELS, DOUBLE_XP, BROADCAST_TIP }

    private void tick() {
        try {
            Map<Integer, Habbo> online = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos();
            if (online == null || online.isEmpty()) {
                return; // skip ticks with no audience — also avoids cron alerts when empty
            }
            EventKind kind = pickEvent();
            switch (kind) {
                case FREE_CREDITS: runFreeCredits(online); break;
                case FREE_PIXELS:  runFreePixels(online);  break;
                case DOUBLE_XP:    runDoubleXp(online);    break;
                case BROADCAST_TIP: runBroadcastTip(online); break;
            }
        } catch (Exception e) {
            LOGGER.error("HotelEvents tick failed", e);
        }
    }

    private EventKind pickEvent() {
        EventKind[] all = EventKind.values();
        return all[ThreadLocalRandom.current().nextInt(all.length)];
    }

    private static final String[] TIPS = new String[] {
            "Tip: complete your daily streak to earn battle pass XP.",
            "Did you know? Trading completes give you XP for both sides.",
            "Friends online? Visit their room from the navigator.",
            "Heads up: a new event is on its way. Stay tuned!"
    };

    private void runBroadcastTip(Map<Integer, Habbo> online) {
        String msg = TIPS[ThreadLocalRandom.current().nextInt(TIPS.length)];
        broadcast(online, msg);
        AuditLog.record(0, "HotelEvents", "HOTEL_EVENT", "tip", "users=" + online.size());
    }

    private void runFreeCredits(Map<Integer, Habbo> online) {
        int amount = Math.max(1, Emulator.getConfig().getInt("hotel.events.free_credits.amount", 50));
        for (Habbo h : online.values()) {
            if (h == null) continue;
            try { h.giveCredits(amount); } catch (Exception ignored) {}
        }
        broadcast(online, "🎁 Hotel event: +" + amount + " credits for every online user!");
        AuditLog.record(0, "HotelEvents", "HOTEL_EVENT", "free_credits", "amount=" + amount + " users=" + online.size());
    }

    private void runFreePixels(Map<Integer, Habbo> online) {
        int amount = Math.max(1, Emulator.getConfig().getInt("hotel.events.free_pixels.amount", 100));
        for (Habbo h : online.values()) {
            if (h == null) continue;
            try { h.givePixels(amount); } catch (Exception ignored) {}
        }
        broadcast(online, "🎁 Hotel event: +" + amount + " duckets for every online user!");
        AuditLog.record(0, "HotelEvents", "HOTEL_EVENT", "free_pixels", "amount=" + amount + " users=" + online.size());
    }

    private void runDoubleXp(Map<Integer, Habbo> online) {
        double factor = Math.max(1.0, Emulator.getConfig().getDouble("hotel.events.double_xp.factor", 2.0));
        int minutes = Math.max(5, Emulator.getConfig().getInt("hotel.events.double_xp.minutes", 60));
        int now = Emulator.getIntUnixTimestamp();
        eventXpMultiplier.set(factor);
        eventXpMultiplierExpiresAt.set(now + minutes * 60L);
        broadcast(online, "⚡ Hotel event: " + String.format("%.1f", factor) + "× battle-pass XP for the next " + minutes + " minutes!");
        AuditLog.record(0, "HotelEvents", "HOTEL_EVENT", "double_xp", "factor=" + factor + " minutes=" + minutes + " users=" + online.size());
    }

    private void broadcast(Map<Integer, Habbo> online, String text) {
        try {
            // Mirror what :hotelalert (HotelAlertCommand) uses, so the toast surface
            // is the same one users already know.
            com.eu.habbo.messages.ServerMessage msg =
                    new StaffAlertWithLinkComposer(text, "").compose();
            for (Habbo h : online.values()) {
                if (h == null || h.getClient() == null) continue;
                if (h.getHabboStats() != null && h.getHabboStats().blockStaffAlerts) continue;
                try { h.getClient().sendResponse(msg); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            LOGGER.warn("HotelEvents broadcast failed", e);
        }
    }
}
