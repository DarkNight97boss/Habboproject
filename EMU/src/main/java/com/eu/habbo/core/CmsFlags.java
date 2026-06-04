package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lettore dei feature flag del CMS lato EMU — direzione CMS -> EMU del ponte
 * (complementare a {@link CmsEventPublisher} che fa EMU -> CMS).
 *
 * Polla periodicamente GET {@code cms.flags.url} (di norma
 * http://127.0.0.1:8092/api/v2/flags) che ritorna {@code {"flags":{"key":true,...}}}
 * (solo i flag ATTIVI, audience non-staff) e mantiene una cache in memoria.
 * Le feature in-game possono chiedere {@code CmsFlags.isEnabled("happy_hour")}
 * per accendersi/spegnersi dallo staff via CMS senza redeploy dell'EMU.
 *
 * Garanzie:
 *  - poller su thread daemon dedicato: MAI sul game thread;
 *  - cache {@code volatile} letta senza lock (read economica e sicura);
 *  - tutto try/catch: CMS irraggiungibile -> la cache resta com'era;
 *  - no-op se {@code cms.flags.url} e' vuoto -> deployare senza configurare e' sicuro.
 *
 * Config (config.ini):
 *  - cms.flags.url              URL del set flag (vuoto -> disattivato)
 *  - cms.flags.refresh.seconds  intervallo refresh (default 30, min 15)
 *  - cms.flags.timeout.ms       timeout HTTP (default 3000)
 */
public final class CmsFlags {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsFlags.class);

    private static volatile Map<String, Boolean> CACHE = Collections.emptyMap();
    private static volatile boolean loadedOnce = false;

    private static volatile boolean started = false;

    private CmsFlags() {}

    /** Avvia il poller. Chiamato dal boot dell'EMU. Idempotente. */
    public static synchronized void start() {
        if (started) return;
        started = true;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cms-flags");
            t.setDaemon(true);
            return t;
        });
        int interval = Math.max(15, Emulator.getConfig().getInt("cms.flags.refresh.seconds", 30));
        scheduler.scheduleWithFixedDelay(CmsFlags::refresh, 0, interval, TimeUnit.SECONDS);
        LOGGER.info("CmsFlags: poller avviato (refresh ogni {}s)", interval);
    }

    /** True se il flag e' presente ed attivo nell'ultima cache letta dal CMS. Default false. */
    public static boolean isEnabled(String key) {
        return Boolean.TRUE.equals(CACHE.get(key));
    }

    private static void refresh() {
        HttpURLConnection conn = null;
        try {
            final String url = Emulator.getConfig().getValue("cms.flags.url", "");
            if (url == null || url.isEmpty()) return; // disattivato

            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            int timeoutMs = Emulator.getConfig().getInt("cms.flags.timeout.ms", 3000);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "AsteriaCore-CmsFlags/1");

            if (conn.getResponseCode() != 200) return;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
            JsonObject flags = (root.has("flags") && root.get("flags").isJsonObject()) ? root.getAsJsonObject("flags") : null;

            Map<String, Boolean> next = new HashMap<>();
            if (flags != null) {
                for (Map.Entry<String, JsonElement> e : flags.entrySet()) {
                    try {
                        next.put(e.getKey(), e.getValue().getAsBoolean());
                    } catch (Throwable ignored) {
                        // valore non booleano: salta
                    }
                }
            }
            CACHE = next;

            if (!loadedOnce) {
                loadedOnce = true;
                LOGGER.info("CmsFlags: caricati {} feature flag attivi dal CMS", next.size());
            }
        } catch (Throwable t) {
            LOGGER.warn("CmsFlags: refresh fallito: {}", t.toString());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
