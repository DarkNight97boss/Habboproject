package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Anti-multiaccount / ban-evasion detection via device fingerprint.
 *
 * The nitro client submits a canonical bundle (userAgent + platform + screen
 * geometry + timezone offset + language + a canvas-rendered hash). The server
 * SHA-256s the concatenation and stores the resulting hash in {@code
 * user_fingerprints} alongside the user_id, IP and machineId.
 *
 * When the same fingerprint_hash is associated with at least
 * {@code fingerprint.alert_min_users} distinct user_ids inside a window of
 * {@code fingerprint.alert_window_days}, an audit log entry of type
 * {@code MULTIACCOUNT_DETECTED} is recorded for staff review.
 *
 * Detection only — no automatic blocking, no client-visible effect. Feature
 * gated by {@code fingerprint.enabled} (default on).
 */
public final class DeviceFingerprint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceFingerprint.class);

    private DeviceFingerprint() {
    }

    public static boolean isEnabled() {
        return Emulator.getConfig().getBoolean("fingerprint.enabled", true);
    }

    /**
     * Server-side hashes the client-supplied components into a stable 64-char
     * hex digest. Returns the digest or an empty string on hashing failure.
     */
    public static String computeHash(String userAgent, String platform, String screen,
                                     String timezone, String language, String canvasHash) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(safe(userAgent)).append('|')
              .append(safe(platform)).append('|')
              .append(safe(screen)).append('|')
              .append(safe(timezone)).append('|')
              .append(safe(language)).append('|')
              .append(safe(canvasHash));
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e) {
            LOGGER.error("DeviceFingerprint hash failed", e);
            return "";
        }
    }

    private static String safe(String s) {
        if (s == null) return "";
        // Strip newlines/separators that would let a malicious client forge a
        // boundary and collide with another canonical payload.
        return s.replace('\n', ' ').replace('\r', ' ').replace('|', '_');
    }

    /**
     * Records the fingerprint for the current session and triggers a
     * multi-account alert (audit log) if the same hash already belongs to a
     * different user inside the alert window.
     */
    public static void record(GameClient client, String fingerprintHash, String userAgent) {
        if (client == null || client.getHabbo() == null || client.getHabbo().getHabboInfo() == null) return;
        if (fingerprintHash == null || fingerprintHash.length() != 64) return;

        int userId = client.getHabbo().getHabboInfo().getId();
        String username = client.getHabbo().getHabboInfo().getUsername();
        String ip = client.getHabbo().getHabboInfo().getIpLogin();
        String machineId = client.getMachineId() == null ? "" : client.getMachineId();
        int now = Emulator.getIntUnixTimestamp();
        String uaTrunc = userAgent == null ? "" : (userAgent.length() > 510 ? userAgent.substring(0, 510) : userAgent);

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO user_fingerprints (user_id, fingerprint_hash, ip, machine_id, user_agent, first_seen, last_seen, seen_count) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 1) " +
                            "ON DUPLICATE KEY UPDATE last_seen = VALUES(last_seen), seen_count = seen_count + 1, " +
                            "ip = VALUES(ip), machine_id = VALUES(machine_id), user_agent = VALUES(user_agent)")) {
                ps.setInt(1, userId);
                ps.setString(2, fingerprintHash);
                ps.setString(3, ip == null ? "" : ip);
                ps.setString(4, machineId);
                ps.setString(5, uaTrunc);
                ps.setInt(6, now);
                ps.setInt(7, now);
                ps.executeUpdate();
            }

            int windowDays = Emulator.getConfig().getInt("fingerprint.alert_window_days", 30);
            int minUsers = Emulator.getConfig().getInt("fingerprint.alert_min_users", 2);
            int cutoff = now - windowDays * 24 * 3600;

            List<Integer> linked = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT DISTINCT user_id FROM user_fingerprints WHERE fingerprint_hash = ? AND last_seen >= ? LIMIT 50")) {
                ps.setString(1, fingerprintHash);
                ps.setInt(2, cutoff);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        linked.add(rs.getInt(1));
                    }
                }
            }

            if (linked.size() >= minUsers) {
                AuditLog.record(userId, username, "MULTIACCOUNT_DETECTED",
                        "fp:" + fingerprintHash.substring(0, 16),
                        "users=" + linked + " ip=" + (ip == null ? "" : ip) + " mid=" + machineId);
            }
        } catch (Exception e) {
            LOGGER.error("DeviceFingerprint.record failed for user {}", userId, e);
        }
    }

    /**
     * Returns the list of user_ids linked to this user via shared fingerprints
     * inside the alert window. Excludes the user themselves. Useful for a future
     * staff dashboard.
     */
    public static List<Integer> linkedUsers(int userId) {
        List<Integer> out = new ArrayList<>();
        int windowDays = Emulator.getConfig().getInt("fingerprint.alert_window_days", 30);
        int cutoff = Emulator.getIntUnixTimestamp() - windowDays * 24 * 3600;
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT DISTINCT b.user_id FROM user_fingerprints a JOIN user_fingerprints b " +
                             "ON a.fingerprint_hash = b.fingerprint_hash AND a.user_id <> b.user_id " +
                             "WHERE a.user_id = ? AND a.last_seen >= ? AND b.last_seen >= ? LIMIT 100")) {
            ps.setInt(1, userId);
            ps.setInt(2, cutoff);
            ps.setInt(3, cutoff);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getInt(1));
            }
        } catch (Exception e) {
            LOGGER.error("DeviceFingerprint.linkedUsers failed", e);
        }
        return out;
    }
}
