package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Happy Hour (#5) — bonus crediti periodico per gli utenti online, gated dal
 * feature flag CMS {@code happy_hour} (via {@link CmsFlags}) e limitato a una
 * finestra oraria. Primo uso reale della direzione flag CMS -> EMU.
 *
 * Ogni {@code happy_hour.interval.minutes} (default 30), SE:
 *   - il flag {@code happy_hour} è ON sul CMS, E
 *   - l'ora locale è nella finestra [{@code happy_hour.start.hour},
 *     {@code happy_hour.end.hour}) (default 18-23),
 * regala {@code happy_hour.reward.credits} (default 5, cap 1000) a ogni utente
 * online e lo avvisa in chat. Lo staff accende/spegne la feature dal CMS senza
 * redeploy dell'EMU.
 *
 * Faucet modesto e controllato (default 5 cr/30min, solo nella finestra). Tutto
 * su thread daemon + try/catch: mai sul game thread, un errore non propaga.
 *
 * Config (config.ini, tutti con default sensati -> nessuna config obbligatoria):
 *   happy_hour.interval.minutes (30) · happy_hour.start.hour (18)
 *   happy_hour.end.hour (23)         · happy_hour.reward.credits (5)
 */
public final class HappyHour {

    private static final Logger LOGGER = LoggerFactory.getLogger(HappyHour.class);
    private static volatile boolean started = false;

    private HappyHour() {}

    /** Avvia lo scheduler. Chiamato dal boot dell'EMU. Idempotente. */
    public static synchronized void start() {
        if (started) return;
        started = true;
        int interval = Math.max(5, Emulator.getConfig().getInt("happy_hour.interval.minutes", 30));
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "happy-hour");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(HappyHour::tick, interval, interval, TimeUnit.MINUTES);
        LOGGER.info("HappyHour: scheduler avviato (controllo ogni {}min)", interval);
    }

    private static void tick() {
        try {
            if (!CmsFlags.isEnabled("happy_hour")) return;

            int startHour = Emulator.getConfig().getInt("happy_hour.start.hour", 18);
            int endHour = Emulator.getConfig().getInt("happy_hour.end.hour", 23);
            int hour = LocalTime.now().getHour();
            if (hour < startHour || hour >= endHour) return; // fuori dalla finestra oraria

            // Cap di sicurezza contro misconfig: mai oltre 1000 crediti per tick.
            int reward = Math.max(0, Math.min(1000, Emulator.getConfig().getInt("happy_hour.reward.credits", 5)));
            if (reward == 0) return;

            int n = 0;
            for (Map.Entry<Integer, Habbo> set : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
                Habbo habbo = set.getValue();
                if (habbo == null) continue;
                try {
                    habbo.giveCredits(reward);
                    if (habbo.getHabboInfo().getCurrentRoom() != null) {
                        habbo.whisper("🎉 Happy Hour! +" + reward + " crediti!", RoomChatMessageBubbles.ALERT);
                    }
                    n++;
                } catch (Throwable ignored) {
                    // un singolo utente non deve fermare il giro
                }
            }
            if (n > 0) {
                LOGGER.info("HappyHour: +{} crediti a {} utenti online", reward, n);
            }
        } catch (Throwable t) {
            LOGGER.warn("HappyHour: tick fallito: {}", t.toString());
        }
    }
}
