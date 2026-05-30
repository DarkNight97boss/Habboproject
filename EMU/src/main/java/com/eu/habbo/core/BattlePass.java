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
import java.util.HashSet;
import java.util.Set;

/**
 * Seasonal Battle Pass (free + premium track, XP-based).
 *
 * Players earn XP from login (+50/day), daily streak claim (+100), and
 * completed trades (+25 per side). Every {@code battlepass.xp_per_tier} XP they
 * unlock the next tier. Each tier has a free reward; if the user purchased the
 * premium pass ({@code battlepass.premium_cost_diamonds} diamonds), they also
 * unlock a premium reward on every tier.
 *
 * MVP: one auto-created season ({@code battlepass.season_days}), 20 tiers
 * hardcoded in {@link #TIERS}.
 */
public final class BattlePass {

    private static final Logger LOGGER = LoggerFactory.getLogger(BattlePass.class);

    public static final int KIND_CREDITS = 0;
    public static final int KIND_PIXELS = 1;
    public static final int KIND_DIAMONDS = 2;
    public static final int KIND_BADGE = 3;

    public static final class Tier {
        public final int tier;
        public final int freeKind;
        public final int freeAmount;
        public final String freeBadge;
        public final int premiumKind;
        public final int premiumAmount;
        public final String premiumBadge;
        public Tier(int tier, int freeKind, int freeAmount, String freeBadge,
                    int premiumKind, int premiumAmount, String premiumBadge) {
            this.tier = tier;
            this.freeKind = freeKind; this.freeAmount = freeAmount; this.freeBadge = freeBadge == null ? "" : freeBadge;
            this.premiumKind = premiumKind; this.premiumAmount = premiumAmount; this.premiumBadge = premiumBadge == null ? "" : premiumBadge;
        }
    }

    public static final Tier[] TIERS = new Tier[] {
            new Tier(1,  KIND_CREDITS,  100, "",  KIND_CREDITS,  300, ""),
            new Tier(2,  KIND_PIXELS,   100, "",  KIND_DIAMONDS, 1,   ""),
            new Tier(3,  KIND_CREDITS,  150, "",  KIND_CREDITS,  500, ""),
            new Tier(4,  KIND_PIXELS,   200, "",  KIND_PIXELS,   600, ""),
            new Tier(5,  KIND_CREDITS,  200, "",  KIND_BADGE,    1,   "BP_S1_BRONZE"),
            new Tier(6,  KIND_PIXELS,   150, "",  KIND_DIAMONDS, 2,   ""),
            new Tier(7,  KIND_CREDITS,  300, "",  KIND_CREDITS,  800, ""),
            new Tier(8,  KIND_PIXELS,   250, "",  KIND_PIXELS,   900, ""),
            new Tier(9,  KIND_CREDITS,  300, "",  KIND_DIAMONDS, 3,   ""),
            new Tier(10, KIND_CREDITS,  500, "",  KIND_BADGE,    1,   "BP_S1_SILVER"),
            new Tier(11, KIND_PIXELS,   300, "",  KIND_CREDITS,  1000, ""),
            new Tier(12, KIND_CREDITS,  400, "",  KIND_DIAMONDS, 3,   ""),
            new Tier(13, KIND_PIXELS,   400, "",  KIND_PIXELS,   1500, ""),
            new Tier(14, KIND_CREDITS,  500, "",  KIND_CREDITS,  1500, ""),
            new Tier(15, KIND_DIAMONDS, 1,   "",  KIND_BADGE,    1,   "BP_S1_GOLD"),
            new Tier(16, KIND_CREDITS,  600, "",  KIND_PIXELS,   2000, ""),
            new Tier(17, KIND_PIXELS,   500, "",  KIND_DIAMONDS, 5,   ""),
            new Tier(18, KIND_CREDITS,  800, "",  KIND_CREDITS,  2500, ""),
            new Tier(19, KIND_PIXELS,   700, "",  KIND_DIAMONDS, 5,   ""),
            new Tier(20, KIND_CREDITS,  1500, "", KIND_BADGE,    1,   "BP_S1_DIAMOND"),
    };

    private BattlePass() {
    }

    public static boolean isEnabled() {
        return Emulator.getConfig().getBoolean("battlepass.enabled", true);
    }

    public static int xpPerTier() {
        return Math.max(50, Emulator.getConfig().getInt("battlepass.xp_per_tier", 500));
    }

    public static int premiumCostDiamonds() {
        return Math.max(0, Emulator.getConfig().getInt("battlepass.premium_cost_diamonds", 50));
    }

    /** Returns the active season id, creating a default one if none exists. */
    public static int ensureActiveSeason() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id FROM battlepass_seasons WHERE active = 1 ORDER BY id DESC LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
            int now = Emulator.getIntUnixTimestamp();
            int days = Math.max(7, Emulator.getConfig().getInt("battlepass.season_days", 30));
            String name = Emulator.getConfig().getValue("battlepass.season_name", "Season 1");
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO battlepass_seasons (name, start_unix, end_unix, active) VALUES (?, ?, ?, 1)",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setInt(2, now);
                ps.setInt(3, now + days * 24 * 3600);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (Exception e) {
            LOGGER.error("BattlePass.ensureActiveSeason fallito", e);
        }
        return 0;
    }

    public static final class SeasonInfo {
        public final int id;
        public final String name;
        public final int startUnix;
        public final int endUnix;
        public SeasonInfo(int id, String name, int startUnix, int endUnix) {
            this.id = id; this.name = name; this.startUnix = startUnix; this.endUnix = endUnix;
        }
    }

    public static SeasonInfo activeSeason() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT id, name, start_unix, end_unix FROM battlepass_seasons WHERE active = 1 ORDER BY id DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new SeasonInfo(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4));
            }
        } catch (Exception e) {
            LOGGER.error("BattlePass.activeSeason fallito", e);
        }
        return null;
    }

    public static final class Progress {
        public final int xp;
        public final boolean premium;
        public final Set<Integer> claimedFree;
        public final Set<Integer> claimedPremium;
        public Progress(int xp, boolean premium, Set<Integer> claimedFree, Set<Integer> claimedPremium) {
            this.xp = xp; this.premium = premium;
            this.claimedFree = claimedFree; this.claimedPremium = claimedPremium;
        }
        public int currentTier() {
            return Math.min(TIERS.length, xp / xpPerTier());
        }
    }

    private static Set<Integer> parseClaimed(String csv) {
        Set<Integer> out = new HashSet<>();
        if (csv == null || csv.isEmpty()) return out;
        for (String s : csv.split(",")) {
            try { out.add(Integer.parseInt(s.trim())); } catch (Exception ignored) {}
        }
        return out;
    }

    private static String formatClaimed(Set<Integer> set) {
        if (set.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Integer i : set) {
            if (!first) sb.append(',');
            sb.append(i);
            first = false;
        }
        return sb.toString();
    }

    public static Progress loadProgress(int userId, int seasonId) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT xp, is_premium, claimed_free, claimed_premium FROM battlepass_progress WHERE user_id = ? AND season_id = ?")) {
            ps.setInt(1, userId);
            ps.setInt(2, seasonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Progress(rs.getInt("xp"), rs.getInt("is_premium") == 1,
                            parseClaimed(rs.getString("claimed_free")),
                            parseClaimed(rs.getString("claimed_premium")));
                }
            }
        } catch (Exception e) {
            LOGGER.error("BattlePass.loadProgress fallito", e);
        }
        return new Progress(0, false, new HashSet<>(), new HashSet<>());
    }

    /**
     * Adds XP to the user's current season. Idempotent per source for "daily"
     * sources: pass {@code dailyOnce=true} to gate by date (uses last_daily_xp_date).
     */
    public static synchronized void addXp(Habbo habbo, int amount, boolean dailyOnce) {
        if (habbo == null || !isEnabled() || amount <= 0) return;
        // Apply the live XP multiplier (time-of-week + active hotel event).
        amount = HotelEvents.applyXpMultiplier(amount);
        if (amount <= 0) return;
        int seasonId = ensureActiveSeason();
        if (seasonId == 0) return;
        int userId = habbo.getHabboInfo().getId();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            if (dailyOnce) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT last_daily_xp_date FROM battlepass_progress WHERE user_id = ? AND season_id = ?")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, seasonId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Date d = rs.getDate(1);
                            if (d != null && d.toLocalDate().equals(LocalDate.now())) return;
                        }
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO battlepass_progress (user_id, season_id, xp, last_daily_xp_date) VALUES (?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE xp = xp + VALUES(xp), last_daily_xp_date = VALUES(last_daily_xp_date)")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, seasonId);
                    ps.setInt(3, amount);
                    ps.setDate(4, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO battlepass_progress (user_id, season_id, xp) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE xp = xp + VALUES(xp)")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, seasonId);
                    ps.setInt(3, amount);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            LOGGER.error("BattlePass.addXp fallito per l'utente {}", userId, e);
        }
    }

    /** Convenience: addXp from the daily-streak claim hook. */
    public static void grantStreakXp(Habbo habbo) {
        if (habbo == null) return;
        addXp(habbo, Emulator.getConfig().getInt("battlepass.xp_streak_claim", 100), false);
    }

    /** Convenience: addXp once per day on login. */
    public static void grantDailyLoginXp(Habbo habbo) {
        if (habbo == null) return;
        addXp(habbo, Emulator.getConfig().getInt("battlepass.xp_daily_login", 50), true);
    }

    /** Convenience: addXp on trade completion (call once per partner). */
    public static void grantTradeXp(Habbo habbo) {
        if (habbo == null) return;
        addXp(habbo, Emulator.getConfig().getInt("battlepass.xp_trade_complete", 25), false);
    }

    /**
     * Claim a tier's free or premium reward. Returns the granted tier or null
     * if invalid / already claimed / not unlocked / premium not purchased.
     */
    public static synchronized Tier claim(Habbo habbo, int tierIdx, boolean premium) {
        if (habbo == null || !isEnabled()) return null;
        if (tierIdx < 1 || tierIdx > TIERS.length) return null;
        int seasonId = ensureActiveSeason();
        if (seasonId == 0) return null;
        int userId = habbo.getHabboInfo().getId();

        Progress p = loadProgress(userId, seasonId);
        if (premium && !p.premium) return null;
        if (tierIdx > p.currentTier()) return null;
        Set<Integer> claimed = premium ? p.claimedPremium : p.claimedFree;
        if (claimed.contains(tierIdx)) return null;

        Tier t = TIERS[tierIdx - 1];
        int kind = premium ? t.premiumKind : t.freeKind;
        int amount = premium ? t.premiumAmount : t.freeAmount;
        String badge = premium ? t.premiumBadge : t.freeBadge;

        // Persist BEFORE granting (anti-replay).
        claimed.add(tierIdx);
        String free = premium ? formatClaimed(p.claimedFree) : formatClaimed(claimed);
        String prem = premium ? formatClaimed(claimed) : formatClaimed(p.claimedPremium);
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE battlepass_progress SET claimed_free = ?, claimed_premium = ? " +
                             "WHERE user_id = ? AND season_id = ?")) {
            ps.setString(1, free);
            ps.setString(2, prem);
            ps.setInt(3, userId);
            ps.setInt(4, seasonId);
            int updated = ps.executeUpdate();
            if (updated == 0) return null;
        } catch (Exception e) {
            LOGGER.error("Salvataggio BattlePass.claim fallito", e);
            return null;
        }

        switch (kind) {
            case KIND_CREDITS: habbo.giveCredits(amount); break;
            case KIND_PIXELS: habbo.givePixels(amount); break;
            case KIND_DIAMONDS: habbo.givePoints(5, amount); break;
            case KIND_BADGE:
                if (!badge.isEmpty()) {
                    try { habbo.addBadge(badge); } catch (Exception ignored) {}
                }
                break;
            default: break;
        }
        AuditLog.record(userId, habbo.getHabboInfo().getUsername(), "BATTLEPASS_CLAIM",
                "tier:" + tierIdx + (premium ? ":prem" : ":free"),
                "kind=" + kind + " amount=" + amount + " badge=" + badge);
        return t;
    }

    /**
     * Purchase the premium pass for the current season. Returns true on success.
     * Pays in diamonds via {@code habbo.givePoints(5, -cost)} after a balance check.
     */
    public static synchronized boolean buyPremium(Habbo habbo) {
        if (habbo == null || !isEnabled()) return false;
        int seasonId = ensureActiveSeason();
        if (seasonId == 0) return false;
        int cost = premiumCostDiamonds();
        int userId = habbo.getHabboInfo().getId();

        Progress p = loadProgress(userId, seasonId);
        if (p.premium) return false;

        int diamonds = habbo.getHabboInfo().getCurrencyAmount(5);
        if (diamonds < cost) return false;

        habbo.givePoints(5, -cost);
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO battlepass_progress (user_id, season_id, xp, is_premium) VALUES (?, ?, 0, 1) " +
                             "ON DUPLICATE KEY UPDATE is_premium = 1")) {
            ps.setInt(1, userId);
            ps.setInt(2, seasonId);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Salvataggio BattlePass.buyPremium fallito", e);
            // Best-effort refund — emulator currency add cannot fail.
            habbo.givePoints(5, cost);
            return false;
        }
        AuditLog.record(userId, habbo.getHabboInfo().getUsername(), "BATTLEPASS_BUY_PREMIUM",
                "season:" + seasonId, "cost=" + cost + " diamonds");
        return true;
    }
}
