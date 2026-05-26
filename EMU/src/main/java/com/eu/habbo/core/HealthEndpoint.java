package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Endpoint HTTP minimale per orchestratori (docker-compose healthcheck,
 * k8s readinessProbe, load balancer) e per scraper Prometheus.
 *
 * Bind di default su 127.0.0.1:9090 — MAI esponere a Internet direttamente.
 * Chi vuole le metriche passa da reverse proxy (Caddy/Nginx con basic-auth
 * o IP-restrict) oppure da Prometheus scraper che gira sullo stesso host.
 *
 * Endpoint:
 *   GET /healthz  -> 200 "OK" sempre che il processo viva (liveness)
 *   GET /readyz   -> 200 se DB risponde + Emulator.isReady (readiness)
 *   GET /metrics  -> 200 text/plain Prometheus-compatible (gauge minime)
 *
 * Volutamente niente librerie esterne: usa com.sun.net.httpserver (JDK
 * built-in). Costa ~1MB di memoria e 2 thread. Per metriche piu' ricche
 * (histogram, exemplars) ci sara' Micrometer in Fase B del piano.
 *
 * Config:
 *   health.enabled                (default 1)
 *   health.host                   (default 127.0.0.1)
 *   health.port                   (default 9090)
 *   health.metrics.basic_auth     (default vuoto = niente auth - safe perche'
 *                                  e' loopback; se bind != 127.0.0.1, METTILO)
 */
public final class HealthEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthEndpoint.class);
    private static HttpServer server;

    /** Contatore di eventi salienti, aggiornato da SecurityMonitor / AuditLog. */
    public static final AtomicLong AUDIT_TOTAL = new AtomicLong(0);
    public static final AtomicLong PACKETS_REJECTED = new AtomicLong(0);
    public static final AtomicLong RATELIMIT_HITS = new AtomicLong(0);

    public static void start() {
        boolean enabled;
        int port;
        String host;
        try {
            enabled = Emulator.getConfig().getBoolean("health.enabled", true);
            port = Emulator.getConfig().getInt("health.port", 9090);
            host = Emulator.getConfig().getValue("health.host", "127.0.0.1");
        } catch (Throwable t) {
            // Bootstrap precoce: config non ancora pronto.
            return;
        }
        if (!enabled) {
            LOGGER.info("HealthEndpoint -> disabled");
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/healthz", ex -> respond(ex, 200, "OK\n"));
            server.createContext("/readyz", HealthEndpoint::handleReady);
            server.createContext("/metrics", HealthEndpoint::handleMetrics);
            // Pool minuscolo: i probe sono leggeri e infrequenti.
            server.setExecutor(Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "HealthEndpoint");
                t.setDaemon(true);
                return t;
            }));
            server.start();
            LOGGER.info("HealthEndpoint -> http://{}:{}/{{healthz,readyz,metrics}}", host, port);
        } catch (Exception e) {
            LOGGER.error("HealthEndpoint failed to start", e);
        }
    }

    public static void stop() {
        if (server != null) {
            try {
                server.stop(1);
            } catch (Exception ignored) {
            }
        }
    }

    private static void handleReady(HttpExchange ex) throws IOException {
        boolean ready = Emulator.isReady && !Emulator.isShuttingDown && pingDb();
        respond(ex, ready ? 200 : 503, ready ? "READY\n" : "NOT READY\n");
    }

    private static void handleMetrics(HttpExchange ex) throws IOException {
        respond(ex, 200, prometheusSnapshot());
    }

    private static boolean pingDb() {
        try (Connection c = Emulator.getDatabase().getDataSource().getConnection()) {
            return c.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Snapshot Prometheus-text-format. Tutte gauge per ora.
     * Costo: ~20 letture in memoria, niente DB. ~5ms per call.
     */
    private static String prometheusSnapshot() {
        long online = 0;
        long rooms = 0;
        try {
            if (Emulator.getGameEnvironment() != null
                    && Emulator.getGameEnvironment().getHabboManager() != null) {
                online = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().size();
            }
            if (Emulator.getGameEnvironment() != null
                    && Emulator.getGameEnvironment().getRoomManager() != null) {
                rooms = Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size();
            }
        } catch (Exception ignored) {
        }

        Runtime r = Runtime.getRuntime();
        long heapTotal = r.totalMemory();
        long heapFree = r.freeMemory();
        long heapUsed = heapTotal - heapFree;
        long heapMax = r.maxMemory();
        long threads = Thread.activeCount();
        long uptimeSec = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000L;

        StringBuilder sb = new StringBuilder(1024);
        appendGauge(sb, "habbo_users_online", "Number of authenticated users currently online", online);
        appendGauge(sb, "habbo_rooms_active", "Number of loaded rooms (loaded != populated)", rooms);
        appendGauge(sb, "habbo_jvm_heap_used_bytes", "JVM heap used (bytes)", heapUsed);
        appendGauge(sb, "habbo_jvm_heap_max_bytes", "JVM heap max (-Xmx)", heapMax);
        appendGauge(sb, "habbo_jvm_heap_total_bytes", "JVM heap committed", heapTotal);
        appendGauge(sb, "habbo_jvm_threads_active", "Active Java threads (Thread.activeCount)", threads);
        appendGauge(sb, "habbo_process_uptime_seconds", "Seconds since JVM start", uptimeSec);
        appendGauge(sb, "habbo_audit_entries_total", "Cumulative audit_log entries written since boot", AUDIT_TOTAL.get());
        appendGauge(sb, "habbo_packets_rejected_total", "Packets rejected (ratelimit / unknown header / oversize)", PACKETS_REJECTED.get());
        appendGauge(sb, "habbo_ratelimit_hits_total", "Sliding-window ratelimit kicks (per-IP, per-user)", RATELIMIT_HITS.get());
        return sb.toString();
    }

    private static void appendGauge(StringBuilder sb, String name, String help, long value) {
        sb.append("# HELP ").append(name).append(' ').append(help).append('\n');
        sb.append("# TYPE ").append(name).append(" gauge\n");
        sb.append(name).append(' ').append(value).append('\n');
    }

    private static void respond(HttpExchange ex, int code, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
        ex.getResponseHeaders().add("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }
}
