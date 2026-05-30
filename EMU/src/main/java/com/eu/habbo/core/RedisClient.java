package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Wrapper Redis opzionale, attivato solo se {@code redis.enabled=true} in
 * config.ini. Quando disattivato, {@link #isEnabled()} ritorna false e tutte
 * le operazioni helper sono no-op — l'emulatore continua come single-node
 * senza pagare overhead di connessione.
 *
 * Use case principale (Tier 2 architettura, >1 EMU dietro load balancer):
 *  - {@link RedisRateLimiter}: sliding-window cross-instance per anti-flood
 *    di SSO + MFA. Senza Redis, ogni nodo ha la sua finestra in-memory e un
 *    attaccante puo' ruotare l'indirizzo IP percepito tra i nodi.
 *  - Session/SSO cache condivisa (future): un client che si riconnette puo'
 *    finire su un nodo diverso, e la SSO single-use deve essere visibile a
 *    tutti i nodi.
 *  - Fingerprint cache: lookup recenti senza colpire DB.
 *
 * Implementazione: JedisPool sync (max 32 conn). Per pattern async + cluster
 * passare a Lettuce in Fase D. Jedis 4.4.x e' Java 8 compatibile; 5.x no.
 *
 * Config (config.ini):
 *   redis.enabled            (default 0)
 *   redis.host               (default 127.0.0.1)
 *   redis.port               (default 6379)
 *   redis.password           (default vuoto)
 *   redis.db                 (default 0)
 *   redis.pool.max           (default 32)
 *   redis.pool.max_idle      (default 8)
 *   redis.timeout.ms         (default 2000)
 *   redis.key.prefix         (default "habbo:") namespace per chiavi
 */
public final class RedisClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);

    private static volatile JedisPool pool;
    private static volatile boolean enabled = false;
    private static volatile String prefix = "habbo:";

    private RedisClient() {
    }

    /** Chiamato da Emulator.main al bootstrap (dopo che config e' caricato). */
    public static synchronized void init() {
        if (pool != null) return; // gia' inizializzato
        boolean want;
        try {
            want = Emulator.getConfig().getBoolean("redis.enabled", false);
        } catch (Throwable t) {
            want = false;
        }
        if (!want) {
            LOGGER.info("RedisClient -> disabilitato (redis.enabled=false)");
            return;
        }
        try {
            String host = Emulator.getConfig().getValue("redis.host", "127.0.0.1");
            int port = Emulator.getConfig().getInt("redis.port", 6379);
            String pwd = Emulator.getConfig().getValue("redis.password", "");
            int db = Emulator.getConfig().getInt("redis.db", 0);
            int timeoutMs = Emulator.getConfig().getInt("redis.timeout.ms", 2000);
            prefix = Emulator.getConfig().getValue("redis.key.prefix", "habbo:");

            JedisPoolConfig pc = new JedisPoolConfig();
            pc.setMaxTotal(Emulator.getConfig().getInt("redis.pool.max", 32));
            pc.setMaxIdle(Emulator.getConfig().getInt("redis.pool.max_idle", 8));
            pc.setMinIdle(2);
            pc.setTestOnBorrow(false); // ping-on-borrow troppo costoso in hot path
            pc.setTestWhileIdle(true);
            pc.setBlockWhenExhausted(true);
            pc.setMaxWait(Duration.ofMillis(timeoutMs));

            if (pwd != null && !pwd.isEmpty()) {
                pool = new JedisPool(pc, host, port, timeoutMs, pwd, db);
            } else {
                pool = new JedisPool(pc, host, port, timeoutMs, null, db);
            }

            // Smoke test: PING, log success/failure.
            try (Jedis j = pool.getResource()) {
                String pong = j.ping();
                if ("PONG".equalsIgnoreCase(pong)) {
                    enabled = true;
                    LOGGER.info("RedisClient -> connesso a {}:{}/{} (prefisso='{}')", host, port, db, prefix);
                } else {
                    LOGGER.error("RedisClient -> risposta ping inattesa '{}', resta disabilitato", pong);
                    closeQuietly();
                }
            }
        } catch (Exception e) {
            LOGGER.error("RedisClient -> init fallito, resta disabilitato: {}", e.toString());
            closeQuietly();
        }
    }

    public static boolean isEnabled() {
        return enabled && pool != null && !pool.isClosed();
    }

    /** Namespace di tutte le chiavi (es. "habbo:rl:sso:1.2.3.4"). */
    public static String key(String suffix) {
        return prefix + suffix;
    }

    /** Borrow una connessione dal pool. SEMPRE in try-with-resources. */
    public static Jedis borrow() {
        if (!isEnabled()) throw new IllegalStateException("RedisClient è disabilitato");
        return pool.getResource();
    }

    public static synchronized void close() {
        closeQuietly();
    }

    private static void closeQuietly() {
        try {
            if (pool != null) pool.close();
        } catch (Exception ignored) {
        }
        pool = null;
        enabled = false;
    }
}
