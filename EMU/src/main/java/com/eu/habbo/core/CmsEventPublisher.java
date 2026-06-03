package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Publisher async di eventi di gioco verso il CMS (event-bus EMU -> CMS).
 *
 * POST fire-and-forget verso {@code cms.events.url} (di norma
 * http://127.0.0.1:8092/api/v2/activity/ingest) con header
 * {@code X-Internal-Secret = cms.events.secret}. Body nello schema dell'ingest
 * CMS:  { type, actorId, actorName, payload, visibility }.
 *
 * Stesso pattern (e stesse garanzie) di {@link AlertSink}:
 *  - executor single-thread con coda bounded -> il game thread NON si blocca
 *    mai su una chiamata HTTP lenta;
 *  - coda piena -> evento droppato e loggato WARN (best-effort by design:
 *    il feed CMS non e' una sorgente autoritativa);
 *  - tutto in try/catch: un CMS irraggiungibile non deve MAI influire sul gioco.
 *
 * Disattivato (no-op) finche' {@code cms.events.url} o {@code cms.events.secret}
 * sono vuoti -> deployare questa classe senza configurarla e' sicuro.
 *
 * Config (config.ini):
 *  - cms.events.url         URL dell'endpoint ingest (vuoto -> disattivato)
 *  - cms.events.secret      secret condiviso (== CMS INTERNAL_API_SECRET)
 *  - cms.events.timeout.ms  timeout HTTP (default 3000)
 */
public final class CmsEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsEventPublisher.class);

    // Single-thread + coda bounded: il game thread non si blocca mai; eventi
    // oltre la capacita' della coda vengono scartati.
    private static final ThreadPoolExecutor EXECUTOR;
    static {
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(200);
        EXECUTOR = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queue,
                r -> {
                    Thread t = new Thread(r, "cms-event-publisher");
                    t.setDaemon(true);
                    return t;
                },
                (r, e) -> LOGGER.warn("Coda CmsEventPublisher piena, evento scartato"));
    }

    private CmsEventPublisher() {}

    /**
     * Pubblica un evento di gioco nel feed CMS (fire-and-forget, mai bloccante).
     *
     * @param type      tipo evento (es. "room.created")
     * @param actorId   id utente Arcturus (0 = nessuno)
     * @param actorName username (puo' essere null/vuoto)
     * @param payload   dati extra (puo' essere null)
     */
    public static void publish(String type, int actorId, String actorName, JsonObject payload) {
        try {
            if (type == null || type.isEmpty()) return;

            final String url = Emulator.getConfig().getValue("cms.events.url", "");
            final String secret = Emulator.getConfig().getValue("cms.events.secret", "");
            if (url == null || url.isEmpty() || secret == null || secret.isEmpty()) return; // disattivato

            JsonObject body = new JsonObject();
            body.addProperty("type", type);
            if (actorId > 0) body.addProperty("actorId", actorId);
            if (actorName != null && !actorName.isEmpty()) body.addProperty("actorName", actorName);
            if (payload != null) body.add("payload", payload);
            body.addProperty("visibility", "public");

            EXECUTOR.submit(() -> doPost(url, secret, body));
        } catch (Throwable t) {
            // Non deve MAI propagare nel game thread.
            LOGGER.warn("CmsEventPublisher.publish fallito: {}", t.toString());
        }
    }

    private static void doPost(String urlStr, String secret, JsonObject body) {
        HttpURLConnection conn = null;
        try {
            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            int timeoutMs = Emulator.getConfig().getInt("cms.events.timeout.ms", 3000);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "AsteriaCore-CmsEventPublisher/1");
            conn.setRequestProperty("X-Internal-Secret", secret);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                LOGGER.warn("CmsEventPublisher: ingest ha restituito {}", code);
            }
        } catch (Throwable t) {
            LOGGER.warn("CmsEventPublisher: POST fallito: {}", t.toString());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
