package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Protezione minori (#21) — rilevatore OSSERVAZIONALE di possibili minori in
 * chat, gated dal feature flag CMS {@code minor_protection} (via {@link CmsFlags}).
 *
 * Aiuto allo staff per la tutela dei minori: quando un messaggio contiene una
 * dichiarazione d'eta' sotto soglia (es. "ho 12 anni") o la parola "minorenne",
 * registra UN alert staff (CmsEventPublisher visibility "staff", fuori dal feed
 * pubblico) con cooldown per utente. NON blocca, NON muta, NON prende ALCUNA
 * azione automatica: e' solo un segnale per la revisione umana -> rischio zero
 * in gioco. L'alert compare nel pannello staff (/admin/alerts).
 *
 * <b>Dark-launch</b>: no-op finche' il flag {@code minor_protection} e' OFF.
 *
 * Config: minor.age.threshold (14) · minor.cooldown.seconds (3600)
 */
public final class MinorGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinorGuard.class);
    private static final ConcurrentHashMap<Integer, Long> LAST_ALERT = new ConcurrentHashMap<>();

    // "ho 12 anni", "12 anni", "12 anno", "12 years old", "12 yo" — esclude "12 anni fa".
    private static final Pattern AGE = Pattern.compile(
            "(?<!\\d)(\\d{1,2})\\s*(?:ann[io]|years?\\s*old|y/?o)\\b(?!\\s*fa)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern MINORENNE = Pattern.compile("\\bminorenne\\b", Pattern.CASE_INSENSITIVE);

    private MinorGuard() {}

    /** Osserva un messaggio (non blocca MAI). No-op se il flag e' OFF. */
    public static void observe(Habbo habbo, String rawMessage) {
        try {
            if (habbo == null || rawMessage == null || rawMessage.isEmpty()) return;
            if (!CmsFlags.isEnabled("minor_protection")) return;

            // Lo staff e' esente (puo' discutere casi senza auto-segnalarsi).
            try {
                if (habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) return;
            } catch (Throwable ignored) { }

            int threshold = Math.max(5, Math.min(17, Emulator.getConfig().getInt("minor.age.threshold", 14)));
            int cooldownSec = Math.max(60, Emulator.getConfig().getInt("minor.cooldown.seconds", 3600));

            boolean minorenne = MINORENNE.matcher(rawMessage).find();
            Integer detectedAge = null;
            if (!minorenne) {
                Matcher m = AGE.matcher(rawMessage);
                while (m.find()) {
                    try {
                        int age = Integer.parseInt(m.group(1));
                        if (age >= 4 && age <= threshold) { detectedAge = age; break; }
                    } catch (NumberFormatException ignored) { }
                }
            }
            if (!minorenne && detectedAge == null) return;

            int userId = habbo.getHabboInfo().getId();
            long nowSec = System.currentTimeMillis() / 1000L;
            Long last = LAST_ALERT.get(userId);
            if (last != null && (nowSec - last) < cooldownSec) return;
            LAST_ALERT.put(userId, nowSec);

            String username = habbo.getHabboInfo().getUsername();
            com.google.gson.JsonObject p = new com.google.gson.JsonObject();
            p.addProperty("user", username == null ? "" : username);
            p.addProperty("userId", userId);
            if (detectedAge != null) p.addProperty("age", detectedAge);
            p.addProperty("signal", minorenne ? "minorenne" : "eta");
            CmsEventPublisher.publish("minor.flag", userId, username, p, "staff");
            LOGGER.info("MinorGuard: possibile minore — utente '{}' (id {}), segnale {}{}",
                    username, userId, minorenne ? "minorenne" : "eta",
                    detectedAge != null ? " (" + detectedAge + ")" : "");
        } catch (Throwable ignored) {
            // osservazionale + best-effort: non deve mai influire sulla chat
        }
    }

    /** Libera lo stato alla disconnessione (memoria bounded). */
    public static void onDisconnect(int userId) {
        LAST_ALERT.remove(userId);
    }
}
