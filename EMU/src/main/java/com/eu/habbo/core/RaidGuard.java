package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rilevamento raid/flood (#20) — ADDITIVO e gated dal feature flag CMS
 * {@code raid_detection} (via {@link CmsFlags}).
 *
 * Osserva la frequenza di ingressi per stanza (finestra scorrevole); se troppi
 * ingressi in poco tempo, registra un alert STAFF (CmsEventPublisher visibility
 * "staff") con cooldown per stanza + un WARN nel log. NON modifica lo stato
 * della stanza: solo osserva e segnala → nessun rischio per i giocatori.
 *
 * **Dark-launch**: no-op finché il flag {@code raid_detection} è OFF (default).
 * Lo staff lo accende dal CMS e lo collauda quando vuole.
 *
 * Config: raid.window.seconds (10) · raid.threshold (12) · raid.cooldown.seconds (120)
 */
public final class RaidGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaidGuard.class);

    private static final Map<Integer, Deque<Long>> JOINS = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> LAST_ALERT = new ConcurrentHashMap<>();

    private RaidGuard() {}

    /** Chiamato a ogni ingresso stanza. No-op se il flag è OFF (dark-launch). */
    public static void onJoin(int roomId, String roomName) {
        try {
            if (!CmsFlags.isEnabled("raid_detection")) return; // spento di default

            int windowSec = Math.max(2, Emulator.getConfig().getInt("raid.window.seconds", 10));
            int threshold = Math.max(3, Emulator.getConfig().getInt("raid.threshold", 12));
            int cooldownSec = Math.max(10, Emulator.getConfig().getInt("raid.cooldown.seconds", 120));
            long now = System.currentTimeMillis();
            long windowMs = windowSec * 1000L;

            Deque<Long> dq = JOINS.computeIfAbsent(roomId, k -> new ArrayDeque<>());
            int count;
            synchronized (dq) {
                dq.addLast(now);
                while (!dq.isEmpty() && (now - dq.peekFirst()) > windowMs) dq.pollFirst();
                count = dq.size();
            }

            if (count >= threshold) {
                Long last = LAST_ALERT.get(roomId);
                if (last == null || (now - last) > cooldownSec * 1000L) {
                    LAST_ALERT.put(roomId, now);
                    com.google.gson.JsonObject p = new com.google.gson.JsonObject();
                    p.addProperty("room", roomName == null ? "" : roomName);
                    p.addProperty("roomId", roomId);
                    p.addProperty("joins", count);
                    p.addProperty("seconds", windowSec);
                    CmsEventPublisher.publish("raid.detected", 0, "", p, "staff");
                    LOGGER.warn("RaidGuard: possibile raid in stanza '{}' (id {}) — {} ingressi in {}s", roomName, roomId, count, windowSec);
                }
            }
        } catch (Throwable ignored) {
            // additivo + best-effort: non deve mai influire sull'ingresso stanza
        }
    }
}
