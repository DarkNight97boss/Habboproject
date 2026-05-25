package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Tamper-evident audit log for sensitive/privileged actions.
 *
 * Each record stores a SHA-256 hash computed over its own fields PLUS the hash of
 * the previous record (a hash-chain). Any later edit/deletion of a row breaks the
 * chain and is therefore detectable: re-hashing the table and comparing against the
 * stored hashes reveals tampering. Writes are serialized to keep the chain consistent.
 *
 * Usage: AuditLog.record(actorId, actorName, "GIVE_CREDITS", "user:42", "amount=1000");
 */
public final class AuditLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLog.class);
    private static final Object LOCK = new Object();
    private static volatile String lastHash = null; // cached tip of the chain (this instance)

    private AuditLog() {
    }

    public static void record(int actorId, String actorName, String action, String target, String detail) {
        if (action == null) action = "";
        if (actorName == null) actorName = "";
        if (target == null) target = "";
        if (detail == null) detail = "";
        if (detail.length() > 512) detail = detail.substring(0, 512);
        if (target.length() > 128) target = target.substring(0, 128);

        synchronized (LOCK) {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
                String prev = lastHash;
                if (prev == null) {
                    try (PreparedStatement ps = connection.prepareStatement("SELECT hash FROM audit_log ORDER BY id DESC LIMIT 1");
                         ResultSet rs = ps.executeQuery()) {
                        prev = rs.next() ? rs.getString(1) : "";
                    }
                }

                int timestamp = Emulator.getIntUnixTimestamp();
                String hash = sha256(timestamp + "|" + actorId + "|" + action + "|" + target + "|" + detail + "|" + prev);

                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO audit_log (timestamp, actor_id, actor_name, action, target, detail, prev_hash, hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, timestamp);
                    ps.setInt(2, actorId);
                    ps.setString(3, actorName);
                    ps.setString(4, action);
                    ps.setString(5, target);
                    ps.setString(6, detail);
                    ps.setString(7, prev);
                    ps.setString(8, hash);
                    ps.execute();
                }

                lastHash = hash;
            } catch (Exception e) {
                LOGGER.error("Failed to write audit log entry", e);
            }
        }
    }

    /**
     * Re-computes the chain from the DB and returns the id of the first tampered row,
     * or 0 if the chain is intact. Intended for an HK "verify integrity" action.
     */
    public static long verifyIntegrity() {
        synchronized (LOCK) {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT id, timestamp, actor_id, action, target, detail, prev_hash, hash FROM audit_log ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {
                String expectedPrev = "";
                while (rs.next()) {
                    String prev = rs.getString("prev_hash");
                    String stored = rs.getString("hash");
                    String recomputed = sha256(rs.getInt("timestamp") + "|" + rs.getInt("actor_id") + "|" + rs.getString("action") + "|" + rs.getString("target") + "|" + rs.getString("detail") + "|" + prev);
                    if (!expectedPrev.equals(prev) || !recomputed.equals(stored)) {
                        return rs.getLong("id");
                    }
                    expectedPrev = stored;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to verify audit log integrity", e);
                return -1;
            }
        }
        return 0;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
