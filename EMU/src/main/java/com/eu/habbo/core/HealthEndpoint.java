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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;

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
    /** Throughput per-feature (#28): messaggi chat (non-comando) e comandi eseguiti dal boot. */
    public static final AtomicLong CHAT_MESSAGES = new AtomicLong(0);
    public static final AtomicLong COMMANDS_RUN = new AtomicLong(0);
    /** Alert moderazione emessi dalle guard (#16/#17/#19/#20/#21) dal boot. */
    public static final AtomicLong MODERATION_FLAGS = new AtomicLong(0);
    /** Connessioni rifiutate dal cap per-IP (ConnectionLimiter) dal boot. */
    public static final AtomicLong CONNECTIONS_REJECTED = new AtomicLong(0);

    /**
     * Histogram della durata di {@code MessageHandler.handle()} (SLI 3 in SLO.md).
     * Popolato da PacketManager per ogni dispatch. Quantili (p50/p95/p99) si
     * calcolano lato PromQL via {@code histogram_quantile()}.
     */
    public static final LatencyHistogram PACKET_PROCESSING_SECONDS =
            new LatencyHistogram("habbo_packet_processing_seconds",
                    "Time spent inside a packet handler.handle() invocation, including I/O it triggers");

    /** Histogram round-trip TCP del synthetic probe (SLI 1 supplementare). */
    public static final LatencyHistogram SYNTHETIC_CONNECT_SECONDS =
            new LatencyHistogram("habbo_synthetic_connect_seconds",
                    "Self-probe TCP connect duration to the game port");

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
            LOGGER.info("HealthEndpoint -> disabilitato");
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/healthz", ex -> respond(ex, 200, "OK\n"));
            HttpContext readyCtx = server.createContext("/readyz", HealthEndpoint::handleReady);
            HttpContext metricsCtx = server.createContext("/metrics", HealthEndpoint::handleMetrics);

            // Basic-auth (security audit P2.7): health.metrics.basic_auth = "user:pass".
            // Era documentata ma MAI implementata: chi bindava fuori da loopback credendosi
            // protetto esponeva metriche e il ping DB (/readyz apre una connessione dal
            // pool) in chiaro. Confronto in tempo costante. /healthz resta aperto: e' la
            // liveness, non contiene dati sensibili.
            String basic = Emulator.getConfig().getValue("health.metrics.basic_auth", "");
            boolean loopback = host.equals("127.0.0.1") || host.equals("::1") || host.equalsIgnoreCase("localhost");
            if (basic != null && basic.contains(":")) {
                final byte[] expUser = basic.substring(0, basic.indexOf(':')).getBytes(StandardCharsets.UTF_8);
                final byte[] expPass = basic.substring(basic.indexOf(':') + 1).getBytes(StandardCharsets.UTF_8);
                BasicAuthenticator auth = new BasicAuthenticator("habbo-health") {
                    @Override
                    public boolean checkCredentials(String user, String pass) {
                        byte[] u = String.valueOf(user).getBytes(StandardCharsets.UTF_8);
                        byte[] p = String.valueOf(pass).getBytes(StandardCharsets.UTF_8);
                        // & (non &&) per non cortocircuitare: tempo costante su entrambi.
                        return MessageDigest.isEqual(expUser, u) & MessageDigest.isEqual(expPass, p);
                    }
                };
                readyCtx.setAuthenticator(auth);
                metricsCtx.setAuthenticator(auth);
                LOGGER.info("HealthEndpoint -> basic-auth attiva su /readyz e /metrics");
            } else if (!loopback) {
                LOGGER.warn("HealthEndpoint bindato su {} SENZA health.metrics.basic_auth: /metrics e /readyz sono esposti in chiaro", host);
            }
            // Pool minuscolo: i probe sono leggeri e infrequenti.
            server.setExecutor(Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "HealthEndpoint");
                t.setDaemon(true);
                return t;
            }));
            server.start();
            LOGGER.info("HealthEndpoint -> http://{}:{}/{{healthz,readyz,metrics}}", host, port);
        } catch (Exception e) {
            LOGGER.error("Avvio HealthEndpoint fallito", e);
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
        appendGauge(sb, "habbo_chat_messages_total", "Chat messages processed (non-command) since boot", CHAT_MESSAGES.get());
        appendGauge(sb, "habbo_commands_executed_total", "Chat commands executed since boot", COMMANDS_RUN.get());
        appendGauge(sb, "habbo_moderation_flags_total", "Moderation alerts emitted by guards (raid/macro/minor/toxicity) since boot", MODERATION_FLAGS.get());
        appendGauge(sb, "habbo_connections_rejected_total", "Connections refused by the per-IP connection cap since boot", CONNECTIONS_REJECTED.get());
        // Histogram (SLI 3 latency + synthetic probe).
        PACKET_PROCESSING_SECONDS.appendTo(sb);
        SYNTHETIC_CONNECT_SECONDS.appendTo(sb);
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
