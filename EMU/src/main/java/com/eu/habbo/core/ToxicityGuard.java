package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Toxicity scoring (#16) — guardiano chat OSSERVAZIONALE che assegna un punteggio
 * di tossicita' al messaggio e, oltre soglia, segnala allo staff. Va OLTRE il
 * wordfilter (match esatto): combina parole vietate (configurabili) + segnali
 * strutturali (CAPS-rage, ripetizioni). Gated dal feature flag CMS
 * {@code toxicity_filter} (via {@link CmsFlags}).
 *
 * NON blocca, NON muta, NON prende azioni: solo un alert staff (visibility
 * "staff", compare in /admin/alerts) con cooldown per utente → rischio zero.
 *
 * Config (config.ini): toxicity.threshold (2) · toxicity.cooldown.seconds (300)
 * · toxicity.words (CSV di parole vietate, default vuoto → solo segnali
 * strutturali finche' lo staff non popola la lista).
 */
public final class ToxicityGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToxicityGuard.class);
    private static final ConcurrentHashMap<Integer, Long> LAST_ALERT = new ConcurrentHashMap<>();

    private ToxicityGuard() {}

    /** Osserva un messaggio (non blocca MAI). No-op se il flag e' OFF. */
    public static void observe(Habbo habbo, String rawMessage) {
        try {
            if (habbo == null || rawMessage == null || rawMessage.isEmpty()) return;
            if (!CmsFlags.isEnabled("toxicity_filter")) return;

            // Lo staff e' esente.
            try {
                if (habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) return;
            } catch (Throwable ignored) { }

            int threshold = Math.max(1, Emulator.getConfig().getInt("toxicity.threshold", 2));
            int cooldownSec = Math.max(30, Emulator.getConfig().getInt("toxicity.cooldown.seconds", 300));

            int score = scoreToxicity(rawMessage);
            if (score < threshold) return;

            int userId = habbo.getHabboInfo().getId();
            long nowSec = System.currentTimeMillis() / 1000L;
            Long last = LAST_ALERT.get(userId);
            if (last != null && (nowSec - last) < cooldownSec) return;
            LAST_ALERT.put(userId, nowSec);

            String username = habbo.getHabboInfo().getUsername();
            String snippet = rawMessage.length() > 80 ? rawMessage.substring(0, 80) : rawMessage;
            com.google.gson.JsonObject p = new com.google.gson.JsonObject();
            p.addProperty("user", username == null ? "" : username);
            p.addProperty("userId", userId);
            p.addProperty("score", score);
            p.addProperty("text", snippet);
            CmsEventPublisher.publish("toxicity.flag", userId, username, p, "staff");
            LOGGER.info("ToxicityGuard: messaggio tossico (score {}) da '{}' (id {})", score, username, userId);
        } catch (Throwable ignored) {
            // osservazionale + best-effort: non deve mai influire sulla chat
        }
    }

    /** Punteggio euristico: parole vietate (config) + CAPS-rage + ripetizioni. */
    private static int scoreToxicity(String msg) {
        int score = 0;
        String lower = msg.toLowerCase();

        // 1) Parole vietate configurabili (CSV in toxicity.words). +2 ciascuna.
        String words = Emulator.getConfig().getValue("toxicity.words", "");
        if (words != null && !words.isEmpty()) {
            for (String w : words.split(",")) {
                String t = w.trim().toLowerCase();
                if (!t.isEmpty() && lower.contains(t)) score += 2;
            }
        }

        // 2) CAPS-rage: alta percentuale di maiuscole su messaggio non corto. +1.
        int letters = 0, caps = 0;
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) caps++;
            }
        }
        if (letters >= 8 && (caps * 100 / Math.max(1, letters)) >= 70) score += 1;

        // 3) Ripetizione eccessiva di un carattere (aaaaaa / !!!!!). +1.
        int run = 1, maxRun = 1;
        for (int i = 1; i < msg.length(); i++) {
            if (msg.charAt(i) == msg.charAt(i - 1)) {
                run++;
                if (run > maxRun) maxRun = run;
            } else {
                run = 1;
            }
        }
        if (maxRun >= 6) score += 1;

        return score;
    }

    /** Libera lo stato alla disconnessione (memoria bounded). */
    public static void onDisconnect(int userId) {
        LAST_ALERT.remove(userId);
    }
}
