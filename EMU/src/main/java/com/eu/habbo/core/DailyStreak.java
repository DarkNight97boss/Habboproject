package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Daily login streak with cycling 7-day rewards.
 *
 * The streak increases by 1 every day a user claims the daily reward; if a day
 * is skipped the streak resets to 1. The visible reward grid is a 7-day cycle
 * that repeats forever; every multiple of 7 days the user also gets a milestone
 * badge ({@code daily_streak.milestone_badge}, default {@code ACH_Login7}).
 *
 * Feature toggle: {@code daily_streak.enabled} (default 1).
 *
 * Persistence: table {@code daily_streak}. In-memory state is recomputed at
 * every claim, so the DB row is the source of truth (no caches to invalidate).
 */
public final class DailyStreak {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyStreak.class);

    /** Reward kinds visible to the client. */
    public static final int REWARD_CREDITS = 0;
    public static final int REWARD_PIXELS = 1;
    public static final int REWARD_DIAMONDS = 2;
    public static final int REWARD_BADGE = 3;

    /** A single day in the 7-day cycle. */
    public static final class Reward {
        public final int day;          // 1..7 position in the cycle
        public final int kind;         // REWARD_*
        public final int amount;       // currency amount (or 1 for a badge)
        public final String badgeCode; // empty unless kind == BADGE

        public Reward(int day, int kind, int amount, String badgeCode) {
            this.day = day;
            this.kind = kind;
            this.amount = amount;
            this.badgeCode = badgeCode == null ? "" : badgeCode;
        }
    }

    /** The 7-day cycle. Index 0 = day 1, ... index 6 = day 7. */
    public static final Reward[] CYCLE = new Reward[] {
            new Reward(1, REWARD_CREDITS, 50, ""),
            new Reward(2, REWARD_CREDITS, 100, ""),
            new Reward(3, REWARD_PIXELS, 50, ""),
            new Reward(4, REWARD_CREDITS, 150, ""),
            new Reward(5, REWARD_DIAMONDS, 1, ""),
            new Reward(6, REWARD_PIXELS, 200, ""),
            new Reward(7, REWARD_CREDITS, 500, ""),
    };

    private DailyStreak() {
    }

    public static boolean isEnabled() {
        return Emulator.getConfig().getBoolean("daily_streak.enabled", true);
    }

    private static String milestoneBadge() {
        return Emulator.getConfig().getValue("daily_streak.milestone_badge", "ACH_Login7");
    }

    /** Server-side snapshot of a user's streak state. */
    public static final class State {
        public final int currentStreak;
        public final int bestStreak;
        public final boolean canClaimToday;
        public final int dayInCycle; // 1..7 — the NEXT day to claim (or the just-claimed)

        public State(int currentStreak, int bestStreak, boolean canClaimToday, int dayInCycle) {
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
            this.canClaimToday = canClaimToday;
            this.dayInCycle = dayInCycle;
        }
    }

    /** Loads the user's state without mutating anything. */
    public static State load(int userId) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT current_streak, best_streak, last_claim_date FROM daily_streak WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new State(0, 0, true, 1);
                }
                int current = rs.getInt("current_streak");
                int best = rs.getInt("best_streak");
                Date last = rs.getDate("last_claim_date");
                LocalDate today = LocalDate.now();
                boolean canClaim;
                int effectiveStreak;
                if (last == null) {
                    canClaim = true;
                    effectiveStreak = current; // 0 — next claim becomes day 1
                } else {
                    LocalDate lastDate = last.toLocalDate();
                    long days = ChronoUnit.DAYS.between(lastDate, today);
                    if (days == 0) {
                        canClaim = false;
                        effectiveStreak = current;
                    } else if (days == 1) {
                        canClaim = true;
                        effectiveStreak = current; // next claim -> current + 1
                    } else {
                        canClaim = true;
                        effectiveStreak = 0; // streak broken; next claim resets to 1
                    }
                }
                int nextDay = ((effectiveStreak) % 7) + 1;
                return new State(current, best, canClaim, nextDay);
            }
        } catch (Exception e) {
            LOGGER.error("DailyStreak.load failed for user {}", userId, e);
            return new State(0, 0, false, 1);
        }
    }

    /**
     * Awards today's claim if eligible. Returns the granted {@link Reward}, or
     * {@code null} if the user already claimed today / feature is off / DB fails.
     * The caller should refresh client state and send a {@code DailyStreakClaimed}
     * packet.
     */
    public static Reward claim(Habbo habbo) {
        if (habbo == null || habbo.getHabboInfo() == null) return null;
        if (!isEnabled()) return null;

        int userId = habbo.getHabboInfo().getId();
        LocalDate today = LocalDate.now();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            int current = 0;
            int best = 0;
            LocalDate last = null;

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT current_streak, best_streak, last_claim_date FROM daily_streak WHERE user_id = ? FOR UPDATE")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        current = rs.getInt("current_streak");
                        best = rs.getInt("best_streak");
                        Date d = rs.getDate("last_claim_date");
                        if (d != null) last = d.toLocalDate();
                    }
                }
            }

            // Eligibility & new streak.
            int newStreak;
            if (last == null) {
                newStreak = 1;
            } else {
                long days = ChronoUnit.DAYS.between(last, today);
                if (days == 0) return null;          // already claimed today
                if (days == 1) newStreak = current + 1;
                else newStreak = 1;                  // streak broken
            }

            int dayInCycle = ((newStreak - 1) % 7) + 1;
            Reward reward = CYCLE[dayInCycle - 1];
            int newBest = Math.max(best, newStreak);

            // Persist BEFORE granting, so a concurrent double-claim can't win the race.
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO daily_streak (user_id, current_streak, best_streak, total_claims, last_claim_unix, last_claim_date) " +
                            "VALUES (?, ?, ?, 1, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE current_streak = VALUES(current_streak), best_streak = VALUES(best_streak), " +
                            "total_claims = total_claims + 1, last_claim_unix = VALUES(last_claim_unix), last_claim_date = VALUES(last_claim_date)")) {
                ps.setInt(1, userId);
                ps.setInt(2, newStreak);
                ps.setInt(3, newBest);
                ps.setInt(4, Emulator.getIntUnixTimestamp());
                ps.setDate(5, Date.valueOf(today));
                ps.executeUpdate();
            }

            // Grant the reward.
            switch (reward.kind) {
                case REWARD_CREDITS:
                    habbo.giveCredits(reward.amount);
                    break;
                case REWARD_PIXELS:
                    habbo.givePixels(reward.amount);
                    break;
                case REWARD_DIAMONDS:
                    habbo.givePoints(5, reward.amount);
                    break;
                default:
                    break;
            }

            // Milestone badge every full cycle.
            if (newStreak > 0 && newStreak % 7 == 0) {
                String badge = milestoneBadge();
                if (!badge.isEmpty()) {
                    try {
                        habbo.addBadge(badge);
                    } catch (Exception ignored) {
                    }
                }
            }

            AuditLog.record(userId, habbo.getHabboInfo().getUsername(), "DAILY_STREAK",
                    "user:" + userId, "streak=" + newStreak + " kind=" + reward.kind + " amount=" + reward.amount);
            return reward;
        } catch (Exception e) {
            LOGGER.error("DailyStreak.claim failed for user {}", userId, e);
            return null;
        }
    }
}
