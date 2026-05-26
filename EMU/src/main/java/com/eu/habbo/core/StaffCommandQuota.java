package com.eu.habbo.core;

import com.eu.habbo.Emulator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-user staff-command quota.
 *
 * Even a fully-trusted staff account can become a problem once it's compromised
 * (stolen session, browser-resident XSS in CMS, etc.) and the attacker starts
 * burning through privileged commands at machine speed. We cap the number of
 * privileged commands a single user can issue in a sliding 60s window. If the
 * quota is exceeded:
 *   - the command is rejected
 *   - an AUDIT row is written so the lead can investigate
 *   - subsequent calls keep failing until the window slides
 *
 * Non-permissioned commands (ordinary user :sit, :wave, etc.) DO NOT count.
 * That keeps the cap low without affecting legitimate play.
 *
 * Config:
 *   sec.staff.cmd.quota.per_minute   (default 60)
 *   sec.staff.cmd.quota.window.seconds (default 60)
 */
public final class StaffCommandQuota {

    private StaffCommandQuota() {}

    /** user_id -> deque of recent command timestamps (millis). */
    private static final ConcurrentHashMap<Integer, Deque<Long>> WINDOW = new ConcurrentHashMap<>();

    /** Last time we wrote an AUDIT row for this user, to avoid spamming the table. */
    private static final ConcurrentHashMap<Integer, Long> LAST_AUDIT = new ConcurrentHashMap<>();
    private static final long AUDIT_THROTTLE_MS = 30_000L;

    /**
     * Returns true if this command should be ALLOWED. Returns false if the
     * caller has burned the quota — in that case the caller should refuse to
     * execute the command and (best-effort) inform the user.
     */
    public static boolean allow(int userId, String username, String commandKey) {
        int limit;
        long windowMs;
        try {
            limit = Math.max(1, Emulator.getConfig().getInt("sec.staff.cmd.quota.per_minute", 60));
            windowMs = Math.max(1000L,
                    Emulator.getConfig().getInt("sec.staff.cmd.quota.window.seconds", 60) * 1000L);
        } catch (Throwable t) {
            limit = 60;
            windowMs = 60_000L;
        }

        long now = System.currentTimeMillis();
        Deque<Long> q = WINDOW.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (q) {
            // Drop entries older than the window.
            while (!q.isEmpty() && now - q.peekFirst() > windowMs) {
                q.pollFirst();
            }
            if (q.size() >= limit) {
                // Throttle audit to avoid an infinite loop logging a flood.
                Long lastAudit = LAST_AUDIT.get(userId);
                if (lastAudit == null || now - lastAudit > AUDIT_THROTTLE_MS) {
                    LAST_AUDIT.put(userId, now);
                    AuditLog.record(userId, username == null ? "?" : username,
                            "STAFF_CMD_QUOTA_EXCEEDED",
                            "cmd:" + (commandKey == null ? "?" : commandKey),
                            "limit=" + limit + " window_ms=" + windowMs);
                }
                return false;
            }
            q.addLast(now);
        }
        return true;
    }

    /** Reset on disconnect so we don't leak memory for transient sessions. */
    public static void onDisconnect(int userId) {
        WINDOW.remove(userId);
        LAST_AUDIT.remove(userId);
    }
}
