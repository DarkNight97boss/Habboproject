package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Sink async per alert in tempo reale (Discord/Slack/altri webhook).
 *
 * Si aggancia agli eventi di {@link AuditLog#record}: ogni voce di audit
 * critica genera un POST HTTP verso il webhook configurato. Il filtro per
 * tipo di evento e' gestito via {@code alerts.webhook.events} (CSV o "*").
 *
 * Garanzie:
 *  - I post sono fire-and-forget su un executor single-threaded con coda
 *    limitata: un endpoint lento NON blocca mai il game thread.
 *  - Se la coda si riempie l'evento viene droppato e loggato come WARN
 *    (best-effort by design: l'AuditLog su DB resta autoritativo).
 *  - Body JSON firmato opzionalmente con HMAC-SHA-256 via
 *    {@code alerts.webhook.secret} -> header {@code X-Habbo-Signature}.
 *    Il ricevitore puo' validare la firma per essere sicuro che la
 *    chiamata venga davvero dall'EMU.
 *
 * Config:
 *  - alerts.webhook.url        URL del webhook (vuoto -> alert disattivati).
 *  - alerts.webhook.events     CSV di event/action ammessi, "*" = tutti.
 *                              Vuoto = nessun alert anche se URL e' impostato.
 *  - alerts.webhook.timeout.ms timeout per la chiamata HTTP (default 3000).
 *  - alerts.webhook.secret     segreto HMAC-SHA-256 per la firma del body
 *                              (opzionale, default vuoto).
 *
 * Esempio config per ricevere SOLO gli eventi di sicurezza critici:
 *   alerts.webhook.url     = https://discord.com/api/webhooks/.../slack
 *   alerts.webhook.events  = MULTIACCOUNT_DETECTED,NEW_DEVICE_LOGIN,PLUGIN_REJECTED,PLUGIN_ALLOWLIST_OFF,RCON
 */
public final class AlertSink {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertSink.class);

    // Single-threaded executor con coda bounded -> il game thread non si blocca mai
    // su un webhook lento; eventi oltre la capacita' della coda vengono droppati.
    private static final ThreadPoolExecutor EXECUTOR;
    static {
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
        EXECUTOR = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queue,
                r -> {
                    Thread t = new Thread(r, "alert-sink");
                    t.setDaemon(true);
                    return t;
                },
                (r, e) -> LOGGER.warn("AlertSink queue full, dropping event"));
    }

    private AlertSink() {}

    /** Convenience: invoca {@link #post(String, JsonObject)} con i campi standard dell'AuditLog. */
    public static void postAudit(int actorId, String actorName, String action, String target, String detail) {
        try {
            if (action == null || action.isEmpty()) return;
            JsonObject fields = new JsonObject();
            fields.addProperty("actor_id", actorId);
            fields.addProperty("actor_name", actorName == null ? "" : actorName);
            fields.addProperty("target", target == null ? "" : target);
            fields.addProperty("detail", detail == null ? "" : detail);
            post(action, fields);
        } catch (Throwable t) {
            LOGGER.warn("AlertSink.postAudit failed: {}", t.toString());
        }
    }

    /** Fire-and-forget. Returns immediately; il POST avviene async. */
    public static void post(String event, JsonObject fields) {
        try {
            final String url = Emulator.getConfig().getValue("alerts.webhook.url", "");
            if (url == null || url.isEmpty()) return;

            final String filter = Emulator.getConfig().getValue("alerts.webhook.events", "");
            if (filter == null || filter.isEmpty()) return;
            if (!"*".equals(filter.trim()) && !matchesFilter(event, filter)) return;

            EXECUTOR.execute(() -> doPost(url, event, fields));
        } catch (Throwable t) {
            LOGGER.warn("AlertSink.post failed for event {}: {}", event, t.toString());
        }
    }

    private static boolean matchesFilter(String event, String filter) {
        if (event == null) return false;
        for (String entry : filter.split(",")) {
            if (entry.trim().equalsIgnoreCase(event)) return true;
        }
        return false;
    }

    private static void doPost(String urlStr, String event, JsonObject fields) {
        HttpURLConnection conn = null;
        try {
            JsonObject body = new JsonObject();
            body.addProperty("event", event == null ? "" : event);
            body.addProperty("ts", System.currentTimeMillis() / 1000L);
            if (fields != null) body.add("fields", fields);

            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            int timeoutMs = Emulator.getConfig().getInt("alerts.webhook.timeout.ms", 3000);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "AsteriaCore-AlertSink/1");
            conn.setDoOutput(true);

            String secret = Emulator.getConfig().getValue("alerts.webhook.secret", "");
            if (secret != null && !secret.isEmpty()) {
                String sig = hmacSha256Hex(secret, payload);
                if (sig != null) conn.setRequestProperty("X-Habbo-Signature", sig);
            }

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                LOGGER.warn("AlertSink: webhook returned {} for event {}", code, event);
            }
        } catch (Throwable t) {
            LOGGER.warn("AlertSink: POST failed for event {}: {}", event, t.toString());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String hmacSha256Hex(String secret, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(payload);
            StringBuilder sb = new StringBuilder(sig.length * 2);
            for (byte b : sig) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
