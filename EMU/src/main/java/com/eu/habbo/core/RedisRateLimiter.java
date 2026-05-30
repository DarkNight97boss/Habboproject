package com.eu.habbo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * Sliding-window rate-limiter cross-instance basato su Redis Sorted Set.
 *
 * E' una controparte di {@link IpRateLimiter} che funziona quando l'emulatore
 * gira su >1 nodo dietro un load balancer. Il bug che chiude:
 *   - in single-instance, IpRateLimiter usa una ConcurrentHashMap locale.
 *   - in multi-instance, attaccante "ruota" l'IP percepito tra i nodi: ogni
 *     nodo conta i suoi fallimenti e nessuno arriva alla soglia. Effettiva
 *     soglia = MAX_FAILURES x N_NODI.
 * Redis ZADD/ZCOUNT/ZREMRANGEBYSCORE rende la finestra globale.
 *
 * Algoritmo (per IP):
 *   key = "habbo:rl:<label>:<ip>"
 *   1. ZREMRANGEBYSCORE key 0 (now - window)        -> drop entry vecchie
 *   2. ZADD key now now                              -> entry corrente
 *   3. ZCOUNT key (now - window) now                 -> count nella finestra
 *   4. PEXPIRE key window+10                         -> auto-cleanup
 *   Se count > soglia: SET "habbo:rl:lock:<label>:<ip>" 1 EX lockout
 *
 * Performance: 1 round-trip TX-pipelined per failure. ~0.5ms su Redis locale.
 *
 * Fallback: se RedisClient e' disabled o Redis e' irraggiungibile, il
 * metodo silenziosamente delega al RateLimiter in-memory passato al costruttore.
 * Cosi' un Redis che cade NON apre una falla di sicurezza ne' rompe l'EMU.
 */
public final class RedisRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRateLimiter.class);

    private final IpRateLimiter fallback;
    private final String label;
    private final int maxFailures;
    private final int windowSec;
    private final int lockoutSec;

    public RedisRateLimiter(IpRateLimiter fallback, String label,
                            int maxFailures, int windowSec, int lockoutSec) {
        this.fallback = fallback;
        this.label = label;
        this.maxFailures = Math.max(2, maxFailures);
        this.windowSec = Math.max(10, windowSec);
        this.lockoutSec = Math.max(60, lockoutSec);
    }

    /** True se l'IP e' lockato cross-instance. */
    public boolean isBlocked(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        if (!RedisClient.isEnabled()) {
            return fallback.isBlocked(ip);
        }
        try (Jedis j = RedisClient.borrow()) {
            return Boolean.TRUE.equals(j.exists(RedisClient.key("rl:lock:" + label + ":" + ip)));
        } catch (Exception e) {
            LOGGER.warn("Redis isBlocked fallito, fallback in-memory: {}", e.toString());
            return fallback.isBlocked(ip);
        }
    }

    /** Reset su successo (login OK / MFA verified). */
    public void onSuccess(String ip) {
        if (ip == null || ip.isEmpty()) return;
        if (!RedisClient.isEnabled()) {
            fallback.onSuccess(ip);
            return;
        }
        try (Jedis j = RedisClient.borrow()) {
            j.del(RedisClient.key("rl:" + label + ":" + ip));
            j.del(RedisClient.key("rl:lock:" + label + ":" + ip));
        } catch (Exception e) {
            LOGGER.warn("Redis onSuccess fallito: {}", e.toString());
            fallback.onSuccess(ip);
        }
    }

    /** Registra un fallimento. True quando questa chiamata ha appena fatto scattare il lockout. */
    public boolean onFailure(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        if (!RedisClient.isEnabled()) {
            return fallback.onFailure(ip);
        }
        long nowMs = System.currentTimeMillis();
        long cutoff = nowMs - windowSec * 1000L;
        String winKey = RedisClient.key("rl:" + label + ":" + ip);
        String lockKey = RedisClient.key("rl:lock:" + label + ":" + ip);

        try (Jedis j = RedisClient.borrow()) {
            // Step 1: pulisci entry fuori finestra + aggiungi entry corrente.
            //         Usiamo MULTI/EXEC per atomicita' (pipeline + tx in Jedis 4.x).
            Transaction tx = j.multi();
            tx.zremrangeByScore(winKey, 0, cutoff);
            tx.zadd(winKey, nowMs, String.valueOf(nowMs));
            tx.zcount(winKey, cutoff, nowMs);
            tx.pexpire(winKey, windowSec * 1000L + 10_000L);
            List<Object> results = tx.exec();

            long failuresInWindow = 0;
            if (results != null && results.size() >= 3 && results.get(2) instanceof Long) {
                failuresInWindow = (Long) results.get(2);
            }

            if (failuresInWindow >= maxFailures) {
                // Set del lock cross-instance + log audit.
                String set = j.set(lockKey, "1", redis.clients.jedis.params.SetParams.setParams()
                        .nx().ex(lockoutSec));
                if ("OK".equals(set)) {
                    try {
                        AuditLog.record(0, "RedisRateLimiter", "IP_LOCKOUT_" + label,
                                "ip:" + ip, "failures=" + failuresInWindow
                                        + " lockout_sec=" + lockoutSec + " mode=redis");
                    } catch (Exception ignored) {
                    }
                    LOGGER.warn("RedisRateLimiter[{}] -> IP {} bloccato per {}s dopo {} fallimenti",
                            label, ip, lockoutSec, failuresInWindow);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.warn("Redis onFailure fallito, fallback in-memory: {}", e.toString());
            return fallback.onFailure(ip);
        }
    }
}
