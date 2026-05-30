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
            LOGGER.error("Hash DeviceFingerprint fallito", e);
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
     *
     * Anti-frame-up: the client-supplied fingerprintHash is XOR-mixed with a
     * server-trusted component (machineId — set by the client at connection time
     * but persisted server-side and tied to this socket). Replaying a victim's
     * canvas+UA bundle from a different machine therefore cannot collide with
     * the victim's stored hash, so the attacker cannot forge a
     * MULTIACCOUNT_DETECTED alert naming an arbitrary user.
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

        // Re-hash including the server-trusted machineId, so a stolen canvas-bundle
        // cannot replay-match another user's fingerprint.
        String effectiveHash = mixServerSalt(fingerprintHash, machineId);

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            // Per-user row cap: refuse to insert a brand-new fingerprint if this
            // user already has too many distinct ones (an attacker rotating one
            // byte of `screen` per submission would otherwise bloat the table
            // unboundedly for a single account).
            int perUserCap = Emulator.getConfig().getInt("fingerprint.max_per_user", 50);
            boolean fireNewDeviceAlert = false; // set true below if this is a NEW fp for an EXISTING user
            boolean rowAlreadyExists;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT 1 FROM user_fingerprints WHERE user_id = ? AND fingerprint_hash = ? LIMIT 1")) {
                ps.setInt(1, userId);
                ps.setString(2, effectiveHash);
                try (ResultSet rs = ps.executeQuery()) {
                    rowAlreadyExists = rs.next();
                }
            }
            if (!rowAlreadyExists) {
                int distinctRows;
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT COUNT(*) FROM user_fingerprints WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        distinctRows = rs.next() ? rs.getInt(1) : 0;
                    }
                }
                if (distinctRows >= perUserCap) {
                    // Cap reached — update last_seen on the most-recent existing row instead.
                    try (PreparedStatement ps = connection.prepareStatement(
                            "UPDATE user_fingerprints SET last_seen = ?, seen_count = seen_count + 1 " +
                                    "WHERE user_id = ? ORDER BY last_seen DESC LIMIT 1")) {
                        ps.setInt(1, now);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                    return;
                }
                // Not capped + new fingerprint for an EXISTING user → schedule a new-device alert.
                // distinctRows == 0 means this is the user's very first fingerprint → no alert.
                fireNewDeviceAlert = distinctRows > 0;
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO user_fingerprints (user_id, fingerprint_hash, ip, machine_id, user_agent, first_seen, last_seen, seen_count) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 1) " +
                            "ON DUPLICATE KEY UPDATE last_seen = VALUES(last_seen), seen_count = seen_count + 1, " +
                            "ip = VALUES(ip), machine_id = VALUES(machine_id), user_agent = VALUES(user_agent)")) {
                ps.setInt(1, userId);
                ps.setString(2, effectiveHash);
                ps.setString(3, ip == null ? "" : ip);
                ps.setString(4, machineId);
                ps.setString(5, uaTrunc);
                ps.setInt(6, now);
                ps.setInt(7, now);
                ps.executeUpdate();
            }

            // New-device alert: tell the user when a fresh device fingerprint joins their account.
            // Best-effort — never fail the login because of the notification path.
            if (fireNewDeviceAlert && Emulator.getConfig().getBoolean("fingerprint.alert.new_device.enabled", true)) {
                notifyNewDevice(client, userId, username, effectiveHash, ip);
            }

            int windowDays = Emulator.getConfig().getInt("fingerprint.alert_window_days", 30);
            int minUsers = Emulator.getConfig().getInt("fingerprint.alert_min_users", 2);
            int cutoff = now - windowDays * 24 * 3600;

            // Threshold check first via COUNT(DISTINCT) so the alert cannot be
            // hidden by an attacker spawning > LIMIT_50 throwaway rows.
            int distinct;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(DISTINCT user_id) FROM user_fingerprints WHERE fingerprint_hash = ? AND last_seen >= ?")) {
                ps.setString(1, effectiveHash);
                ps.setInt(2, cutoff);
                try (ResultSet rs = ps.executeQuery()) {
                    distinct = rs.next() ? rs.getInt(1) : 0;
                }
            }
            if (distinct < minUsers) {
                return;
            }

            // For the audit detail, sample the most recently-seen linked users.
            List<Integer> linked = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT user_id, MAX(last_seen) AS ls FROM user_fingerprints WHERE fingerprint_hash = ? AND last_seen >= ? " +
                            "GROUP BY user_id ORDER BY ls DESC LIMIT 50")) {
                ps.setString(1, effectiveHash);
                ps.setInt(2, cutoff);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        linked.add(rs.getInt(1));
                    }
                }
            }

            AuditLog.record(userId, username, "MULTIACCOUNT_DETECTED",
                    "fp:" + effectiveHash.substring(0, 16),
                    "distinct=" + distinct + " sample=" + linked + " ip=" + (ip == null ? "" : ip) + " mid=" + machineId);
        } catch (Exception e) {
            LOGGER.error("DeviceFingerprint.record fallito per l'utente {}", userId, e);
        }
    }

    /**
     * Best-effort in-game + audit notification when a brand-new device fingerprint
     * is registered for an existing user (i.e. the user already had at least one
     * fingerprint, so this is NOT the user's first ever login). Failures are
     * swallowed: the fingerprint write is the authoritative step, the alert is
     * decoration.
     */
    private static void notifyNewDevice(GameClient client, int userId, String username, String fpHash, String ip) {
        try {
            if (client != null && client.getHabbo() != null) {
                client.getHabbo().alert(
                        "[Sicurezza] Login da un NUOVO dispositivo rilevato.\n" +
                        "Se non sei stato tu, cambia subito la password e contatta lo staff.");
            }
        } catch (Throwable t) {
            LOGGER.warn("Consegna dell'avviso nuovo dispositivo fallita per l'utente {}: {}", userId, t.toString());
        }
        try {
            String fpShort = fpHash != null && fpHash.length() >= 16 ? fpHash.substring(0, 16) : (fpHash == null ? "" : fpHash);
            AuditLog.record(userId, username, "NEW_DEVICE_LOGIN", "fp:" + fpShort, "ip=" + (ip == null ? "" : ip));
        } catch (Throwable t) {
            LOGGER.warn("Audit nuovo dispositivo fallito per l'utente {}: {}", userId, t.toString());
        }
    }

    /**
     * Re-hashes the client-supplied fingerprint together with a server-trusted salt
     * (machineId), so a replayed canvas bundle from a different machine cannot
     * collide with another user's hash. We use SHA-256 again so the result keeps the
     * 64-hex-character shape the schema expects.
     */
    private static String mixServerSalt(String clientHash, String machineSalt) {
        try {
            String composite = clientHash + "|" + (machineSalt == null ? "" : machineSalt);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(composite.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e) {
            return clientHash;
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
            LOGGER.error("DeviceFingerprint.linkedUsers fallito", e);
        }
        return out;
    }
}
