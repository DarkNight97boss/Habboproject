package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-macro (#17) — rilevatore OSSERVAZIONALE di chat automatizzata (bot),
 * gated dal feature flag CMS {@code anti_macro} (via {@link CmsFlags}).
 *
 * Complementare a {@link ChatSpamGuard} (che gestisce rate-flood e messaggi
 * identici ripetuti, con auto-mute): qui cerchiamo il pattern che lo spam-guard
 * NON vede — messaggi (anche tutti diversi) inviati a intervalli troppo
 * REGOLARI, da metronomo, anche sotto la soglia di flood. Gli umani hanno
 * jitter naturale negli intervalli; i bot quasi no.
 *
 * Quando il pattern e' sospetto (abbastanza campioni + coefficiente di
 * variazione degli intervalli molto basso + intervallo medio in una banda
 * "da bot"), registra UN SOLO alert staff (CmsEventPublisher visibility
 * "staff", fuori dal feed pubblico) con cooldown per utente. NON muta, NON
 * blocca, NON tocca il messaggio: pura osservazione -> rischio zero in gioco.
 *
 * <b>Dark-launch</b>: no-op finche' il flag {@code anti_macro} e' OFF (default).
 *
 * Config (config.ini, default sensati -> nessuna config obbligatoria):
 *   macro.samples (6) · macro.cv.maxpct (15) · macro.interval.min.ms (800)
 *   macro.interval.max.ms (20000) · macro.cooldown.seconds (300)
 */
public final class MacroGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger(MacroGuard.class);
    private static final ConcurrentHashMap<Integer, State> STATE = new ConcurrentHashMap<>();
    private static final int MAX_TS = 24;

    private static final class State {
        final Deque<Long> timestamps = new ArrayDeque<>();
        long lastAlertSec = 0L;
    }

    private MacroGuard() {}

    /** Osserva un messaggio di chat (non blocca MAI). No-op se il flag e' OFF. */
    public static void observe(Habbo habbo, String rawMessage) {
        try {
            if (habbo == null) return;
            if (!CmsFlags.isEnabled("anti_macro")) return;

            // Lo staff e' esente: puo' fare eventi/automazioni legittime.
            try {
                if (habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) return;
            } catch (Throwable ignored) { }

            int samples = Math.max(4, Emulator.getConfig().getInt("macro.samples", 6));
            int cvMaxPct = Math.max(1, Emulator.getConfig().getInt("macro.cv.maxpct", 15));
            long minMs = Math.max(100, Emulator.getConfig().getInt("macro.interval.min.ms", 800));
            long maxMs = Math.max(minMs + 1, Emulator.getConfig().getInt("macro.interval.max.ms", 20000));
            int cooldownSec = Math.max(30, Emulator.getConfig().getInt("macro.cooldown.seconds", 300));

            int userId = habbo.getHabboInfo().getId();
            long nowMs = System.currentTimeMillis();

            double mean;
            double cvPct;
            State s = STATE.computeIfAbsent(userId, k -> new State());
            synchronized (s) {
                s.timestamps.addLast(nowMs);
                while (s.timestamps.size() > MAX_TS) s.timestamps.pollFirst();
                if (s.timestamps.size() <= samples) return; // servono almeno samples+1 timestamp

                // Ultimi `samples` intervalli inter-messaggio.
                Long[] ts = s.timestamps.toArray(new Long[0]);
                int n = ts.length;
                double[] iv = new double[samples];
                int idx = 0;
                for (int i = n - samples - 1; i < n - 1; i++) {
                    iv[idx++] = (double) (ts[i + 1] - ts[i]);
                }

                double sum = 0.0;
                for (double v : iv) sum += v;
                mean = sum / samples;
                if (mean < minMs || mean > maxMs) return; // fuori dalla banda "da bot"

                double var = 0.0;
                for (double v : iv) var += (v - mean) * (v - mean);
                var /= samples;
                double sd = Math.sqrt(var);
                cvPct = mean > 0 ? (sd / mean) * 100.0 : 100.0;
                if (cvPct > cvMaxPct) return; // troppa irregolarita' -> umano

                long nowSec = nowMs / 1000L;
                if (s.lastAlertSec > 0 && (nowSec - s.lastAlertSec) < cooldownSec) return;
                s.lastAlertSec = nowSec;
            }

            String username = habbo.getHabboInfo().getUsername();
            com.google.gson.JsonObject p = new com.google.gson.JsonObject();
            p.addProperty("user", username == null ? "" : username);
            p.addProperty("userId", userId);
            p.addProperty("avgIntervalMs", Math.round(mean));
            p.addProperty("jitterPct", Math.round(cvPct));
            CmsEventPublisher.publish("macro.suspected", userId, username, p, "staff");
            LOGGER.info("MacroGuard: chat sospetta da macro — utente '{}' (id {}), intervallo medio {}ms jitter {}%",
                    username, userId, Math.round(mean), Math.round(cvPct));
        } catch (Throwable ignored) {
            // osservazionale + best-effort: non deve mai influire sulla chat
        }
    }

    /** Libera lo stato alla disconnessione (memoria bounded). */
    public static void onDisconnect(int userId) {
        STATE.remove(userId);
    }
}
