package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Tamper-evident audit log for sensitive/privileged actions.
 *
 * Each record stores an HMAC-SHA-256 over its own fields plus the previous row's HMAC
 * (a hash-chain), keyed by a secret loaded from {@code audit.signing.key} in config.ini
 * (or the {@code HABBO_AUDIT_SIGNING_KEY} environment variable). The threat model is a
 * privileged DB writer (compromised CMS / staff with SQL access): without the secret,
 * such an attacker can still edit or insert rows, but cannot recompute a valid chain,
 * so {@link #verifyIntegrity()} detects the tampering. A plain SHA-256 (the previous
 * implementation) is recomputable by anyone with the row contents, so the same DB
 * compromise could rewrite history undetected — defeating the chain's purpose.
 *
 * Writes are serialized to keep the chain consistent.
 *
 * Usage: AuditLog.record(actorId, actorName, "GIVE_CREDITS", "user:42", "amount=1000");
 */
public final class AuditLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLog.class);
    private static final Object LOCK = new Object();
    private static volatile String lastHash = null; // cached tip of the chain (this instance)

    private static volatile SecretKeySpec signingKey = null;
    private static volatile boolean signingKeyResolved = false;

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
                String hash = sign(timestamp + "|" + actorId + "|" + action + "|" + target + "|" + detail + "|" + prev);

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
     *
     * Runs in batches of {@code audit.verify.batch_size} (default 5000) so the
     * global {@link #LOCK} is released between batches; otherwise a multi-million-row
     * table would block every audit write for minutes during verification, and a
     * staff member could DoS the audit pipeline by spamming "verify".
     */
    public static long verifyIntegrity() {
        int batchSize = Math.max(100, Emulator.getConfig().getInt("audit.verify.batch_size", 5000));
        String expectedPrev = "";
        long lastId = 0;
        while (true) {
            long batchTerminator;
            synchronized (LOCK) {
                batchTerminator = -1;
                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                     PreparedStatement ps = connection.prepareStatement("SELECT id, timestamp, actor_id, action, target, detail, prev_hash, hash FROM audit_log WHERE id > ? ORDER BY id ASC LIMIT ?")) {
                    ps.setLong(1, lastId);
                    ps.setInt(2, batchSize);
                    try (ResultSet rs = ps.executeQuery()) {
                        int seen = 0;
                        while (rs.next()) {
                            seen++;
                            String prev = rs.getString("prev_hash");
                            String stored = rs.getString("hash");
                            String recomputed = sign(rs.getInt("timestamp") + "|" + rs.getInt("actor_id") + "|" + rs.getString("action") + "|" + rs.getString("target") + "|" + rs.getString("detail") + "|" + prev);
                            if (!expectedPrev.equals(prev) || !recomputed.equals(stored)) {
                                return rs.getLong("id");
                            }
                            expectedPrev = stored;
                            lastId = rs.getLong("id");
                        }
                        batchTerminator = (seen < batchSize) ? 0 : 1;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to verify audit log integrity", e);
                    return -1;
                }
            }
            if (batchTerminator == 0) {
                return 0; // exhausted, chain intact
            }
            // else loop; lock released between batches so writers can interleave.
        }
    }

    private static SecretKeySpec resolveSigningKey() {
        if (signingKeyResolved) return signingKey;
        synchronized (AuditLog.class) {
            if (signingKeyResolved) return signingKey;

            String key = System.getenv("HABBO_AUDIT_SIGNING_KEY");
            if (key == null || key.isEmpty()) {
                try {
                    key = Emulator.getConfig().getValue("audit.signing.key", "");
                } catch (Throwable t) {
                    key = "";
                }
            }

            if (key == null || key.isEmpty()) {
                LOGGER.error("audit.signing.key is not configured — the audit log hash chain falls back to unkeyed SHA-256 and a DB write can rewrite history undetected. Set audit.signing.key in config.ini (or the HABBO_AUDIT_SIGNING_KEY env var) to a long random secret.");
                signingKey = null;
            } else {
                signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            }
            signingKeyResolved = true;
            return signingKey;
        }
    }

    private static String sign(String input) {
        SecretKeySpec key = resolveSigningKey();
        try {
            if (key != null) {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(key);
                return toHex(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
            }
            // Fallback so we always store SOMETHING — but logged loudly at startup above.
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return "";
        }
    }

    private static String toHex(byte[] digest) {
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
