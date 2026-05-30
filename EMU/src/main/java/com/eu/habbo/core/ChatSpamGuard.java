package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-flood chat guard with auto-mute and progressive escalation.
 *
 * For each user we keep a sliding window of recent message timestamps and the
 * last few messages (lowercased, trimmed) so we can detect:
 *  - rate floods (too many messages in too short a time);
 *  - identical-message spam (4+ identical messages in a row).
 *
 * The first offence of the day mutes the user for the base duration; subsequent
 * offences double, then quadruple it (1×, 2×, 4×).
 *
 * Config (all in {@code emulator_settings}):
 *   chat.spam.enabled              (default 1)
 *   chat.spam.rate.window_ms       (default 10000)
 *   chat.spam.rate.threshold       (default 8)
 *   chat.spam.repeat.threshold     (default 4)
 *   chat.spam.mute.base_seconds    (default 120)
 *   chat.spam.escalation.reset_seconds (default 86400 — 24h)
 *
 * Persistence is intentionally absent: this is a runtime spam-guard. Real
 * sanctions (escalation history) live in the audit log and existing mute table.
 */
public final class ChatSpamGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatSpamGuard.class);

    private static final ConcurrentHashMap<Integer, UserState> STATE = new ConcurrentHashMap<>();

    private static final int MAX_QUEUE = 16;

    private static final class UserState {
        final Deque<Long> timestamps = new ArrayDeque<>();
        final Deque<String> recentMessages = new ArrayDeque<>();
        int offences = 0;
        long lastOffenceUnixSec = 0L;
    }

    private ChatSpamGuard() {
    }

    public static boolean isEnabled() {
        return Emulator.getConfig().getBoolean("chat.spam.enabled", true);
    }

    /**
     * Records the chat event and, if it crosses a threshold, mutes the user and
     * returns true so the caller can suppress the original message.
     *
     * @return true when the message should be dropped (user just got muted).
     */
    public static boolean observe(Habbo habbo, String rawMessage) {
        if (!isEnabled() || habbo == null || rawMessage == null) return false;
        // Staff can spam (e.g. for events) — never mute privileged accounts.
        try {
            if (habbo.hasPermission(com.eu.habbo.habbohotel.permissions.Permission.ACC_SUPPORTTOOL)) return false;
        } catch (Exception ignored) {
        }

        int userId = habbo.getHabboInfo().getId();
        long nowMs = System.currentTimeMillis();
        String normalized = rawMessage.trim().toLowerCase();
        if (normalized.isEmpty()) return false;

        UserState s = STATE.computeIfAbsent(userId, k -> new UserState());

        synchronized (s) {
            // Reset escalation if quiet long enough.
            int escalationReset = Emulator.getConfig().getInt("chat.spam.escalation.reset_seconds", 86400);
            long nowSec = nowMs / 1000L;
            if (s.lastOffenceUnixSec > 0 && (nowSec - s.lastOffenceUnixSec) > escalationReset) {
                s.offences = 0;
            }

            // Maintain sliding windows.
            int windowMs = Math.max(500, Emulator.getConfig().getInt("chat.spam.rate.window_ms", 10000));
            long cutoff = nowMs - windowMs;
            while (!s.timestamps.isEmpty() && s.timestamps.peekFirst() < cutoff) {
                s.timestamps.pollFirst();
            }
            s.timestamps.addLast(nowMs);
            s.recentMessages.addLast(normalized);
            while (s.recentMessages.size() > MAX_QUEUE) s.recentMessages.pollFirst();

            int rateThreshold = Math.max(2, Emulator.getConfig().getInt("chat.spam.rate.threshold", 8));
            int repeatThreshold = Math.max(2, Emulator.getConfig().getInt("chat.spam.repeat.threshold", 4));

            boolean rateExceeded = s.timestamps.size() >= rateThreshold;
            boolean repeatExceeded = countTrailingRepeats(s.recentMessages) >= repeatThreshold;

            if (!rateExceeded && !repeatExceeded) {
                return false;
            }

            // Compute mute duration with escalation: 1×, 2×, 4× …
            int base = Math.max(15, Emulator.getConfig().getInt("chat.spam.mute.base_seconds", 120));
            int factor = 1 << Math.min(3, s.offences); // 1, 2, 4, 8
            int duration = base * factor;
            s.offences = Math.min(8, s.offences + 1);
            s.lastOffenceUnixSec = nowSec;

            // Clear queues so the user isn't re-muted instantly on the same window.
            s.timestamps.clear();
            s.recentMessages.clear();

            try {
                habbo.mute(duration, false);
            } catch (Exception e) {
                LOGGER.warn("ChatSpamGuard: mute() ha sollevato un'eccezione per l'utente {}", userId, e);
            }

            String reason = rateExceeded ? "rate" : "repeat";
            try {
                habbo.whisper(
                        Emulator.getTexts().getValue("chat.spam.muted",
                                "You have been muted for spamming. Slow down."),
                        com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles.ALERT);
            } catch (Exception ignored) {
            }

            AuditLog.record(userId, habbo.getHabboInfo().getUsername(), "AUTO_MUTE_SPAM",
                    "user:" + userId,
                    "reason=" + reason + " duration=" + duration + "s offence=" + s.offences);
            return true;
        }
    }

    /** Counts how many identical messages sit at the tail of the queue. */
    private static int countTrailingRepeats(Deque<String> q) {
        if (q.isEmpty()) return 0;
        String last = q.peekLast();
        int count = 0;
        for (java.util.Iterator<String> it = q.descendingIterator(); it.hasNext(); ) {
            if (last.equals(it.next())) count++;
            else break;
        }
        return count;
    }

    /** Drops per-user state when they disconnect, so memory stays bounded. */
    public static void onDisconnect(int userId) {
        STATE.remove(userId);
    }
}
