package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window per-IP rate-limiter with sticky lockout.
 *
 * Used to defeat brute-force on bounded-entropy surfaces (SSO ticket guessing,
 * MFA codes per-IP, password-recovery requests, …). After {@code maxFailures}
 * failed attempts inside {@code windowSec}, the IP is blocked for
 * {@code lockoutSec}; further attempts are rejected without touching the DB
 * (so a botnet hammering a target can't pin a connection pool either).
 *
 * State is in-memory only — best-effort across restarts. Memory bounded by an
 * LRU-style trim every once in a while (per-IP entries expire when their window
 * is empty and no lockout is active).
 */
public final class IpRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpRateLimiter.class);

    private static final class Bucket {
        final Deque<Long> failures = new ArrayDeque<>();
        long lockedUntilMs = 0L;
    }

    private final ConcurrentHashMap<String, Bucket> map = new ConcurrentHashMap<>();
    private final String label;
    private final int maxFailures;
    private final int windowSec;
    private final int lockoutSec;

    /**
     * @param label       a short identifier shown in audit log (e.g. "SSO", "MFA")
     * @param maxFailures fails inside the window before lockout (>=2)
     * @param windowSec   sliding window length in seconds (>=10)
     * @param lockoutSec  hard lockout after threshold (>=60)
     */
    public IpRateLimiter(String label, int maxFailures, int windowSec, int lockoutSec) {
        this.label = label;
        this.maxFailures = Math.max(2, maxFailures);
        this.windowSec = Math.max(10, windowSec);
        this.lockoutSec = Math.max(60, lockoutSec);
    }

    /** True when this IP is currently locked out and should be refused. */
    public boolean isBlocked(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        Bucket b = map.get(ip);
        if (b == null) return false;
        synchronized (b) {
            return b.lockedUntilMs > System.currentTimeMillis();
        }
    }

    /** Reset on success (e.g. login OK / MFA verified). */
    public void onSuccess(String ip) {
        if (ip == null || ip.isEmpty()) return;
        Bucket b = map.get(ip);
        if (b != null) {
            synchronized (b) {
                b.failures.clear();
                b.lockedUntilMs = 0L;
            }
        }
    }

    /** Record a failure. Returns true when this attempt JUST tripped the lockout. */
    public boolean onFailure(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        Bucket b = map.computeIfAbsent(ip, k -> new Bucket());
        long now = System.currentTimeMillis();
        long cutoff = now - windowSec * 1000L;
        synchronized (b) {
            while (!b.failures.isEmpty() && b.failures.peekFirst() < cutoff) {
                b.failures.pollFirst();
            }
            b.failures.addLast(now);
            if (b.failures.size() >= maxFailures && b.lockedUntilMs <= now) {
                b.lockedUntilMs = now + lockoutSec * 1000L;
                try {
                    AuditLog.record(0, "IpRateLimiter", "IP_LOCKOUT_" + label,
                            "ip:" + ip, "failures=" + b.failures.size() + " lockout_sec=" + lockoutSec);
                } catch (Exception ignored) {
                }
                LOGGER.warn("IpRateLimiter[{}] -> IP {} bloccato per {}s dopo {} fallimenti",
                        label, ip, lockoutSec, b.failures.size());
                return true;
            }
            return false;
        }
    }

    /** Drops expired entries to keep memory bounded. Call from a periodic task if you want. */
    public void prune() {
        long now = System.currentTimeMillis();
        long cutoff = now - windowSec * 1000L;
        map.entrySet().removeIf(entry -> {
            Bucket b = entry.getValue();
            synchronized (b) {
                while (!b.failures.isEmpty() && b.failures.peekFirst() < cutoff) {
                    b.failures.pollFirst();
                }
                return b.failures.isEmpty() && b.lockedUntilMs <= now;
            }
        });
    }

    /** Global instances for the surfaces we currently protect. Config-driven. */
    public static final IpRateLimiter SSO = new IpRateLimiter("SSO",
            Math.max(2, Emulator.getConfig() == null ? 10 : Emulator.getConfig().getInt("sec.ip.sso.max_failures", 10)),
            Emulator.getConfig() == null ? 300 : Math.max(10, Emulator.getConfig().getInt("sec.ip.sso.window_seconds", 300)),
            Emulator.getConfig() == null ? 900 : Math.max(60, Emulator.getConfig().getInt("sec.ip.sso.lockout_seconds", 900)));
}
