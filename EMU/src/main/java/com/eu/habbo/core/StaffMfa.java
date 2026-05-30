package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.handshake.StaffMfaRequiredComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Staff step-up MFA (Google Authenticator / RFC-6238 TOTP).
 *
 * After a staff account logs in, its privileged powers (commands, mod-tool) are
 * locked until the user enters a valid 6-digit code in a Habbo-style popup
 * (see {@link Totp}, the client modal, and {@code GameClient.isStaffMfaLocked()}).
 * This protects the hotel if a staff account/SSO ticket is stolen: the thief has
 * the account but not the authenticator device.
 *
 * Feature is config-gated OFF by default: mfa.staff.enabled (0/1).
 * Optional: mfa.staff.min_rank (treat ranks >= N as staff, 0 = only mod-tool
 * holders), mfa.staff.issuer (label shown in Google Authenticator).
 *
 * State per user lives in table {@code staff_mfa}. The in-session "elevated"
 * flag lives on {@link com.eu.habbo.habbohotel.gameclients.GameClient}.
 */
public final class StaffMfa {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaffMfa.class);

    private StaffMfa() {
    }

    /** Whether the staff MFA feature is switched on. */
    public static boolean isEnabled() {
        return Emulator.getConfig().getBoolean("mfa.staff.enabled", false);
    }

    /** Issuer label shown inside the authenticator app. */
    public static String issuer() {
        return Emulator.getConfig().getValue("mfa.staff.issuer", "Habbo Hotel");
    }

    /**
     * Whether this account should be challenged for MFA. Anyone with mod-tool
     * access is staff; optionally everyone at/above mfa.staff.min_rank too.
     */
    public static boolean isStaffAccount(Habbo habbo) {
        if (habbo == null || habbo.getHabboInfo() == null) {
            return false;
        }
        if (habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) {
            return true;
        }
        int minRank = Emulator.getConfig().getInt("mfa.staff.min_rank", 0);
        return minRank > 0
                && habbo.getHabboInfo().getRank() != null
                && habbo.getHabboInfo().getRank().getId() >= minRank;
    }

    /** True once this user has scanned the QR and confirmed a first code. */
    public static boolean isEnrolled(int userId) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT enrolled FROM staff_mfa WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("enrolled") == 1;
            }
        } catch (Exception e) {
            LOGGER.error("StaffMfa.isEnrolled fallito per l'utente {}", userId, e);
            return false;
        }
    }

    /**
     * Returns the existing Base32 secret for the user, creating (and persisting,
     * enrolled=0) a fresh one if none exists. Never returns null/empty unless the
     * database is unreachable.
     */
    public static String getOrCreateSecret(int userId) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT secret FROM staff_mfa WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String secret = rs.getString("secret");
                        if (secret != null && !secret.isEmpty()) {
                            return secret;
                        }
                    }
                }
            }

            String secret = Totp.generateSecret();
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO staff_mfa (user_id, secret, enrolled, enrolled_at) VALUES (?, ?, 0, 0) " +
                            "ON DUPLICATE KEY UPDATE secret = VALUES(secret)")) {
                ps.setInt(1, userId);
                ps.setString(2, secret);
                ps.executeUpdate();
            }
            return secret;
        } catch (Exception e) {
            LOGGER.error("StaffMfa.getOrCreateSecret fallito per l'utente {}", userId, e);
            return "";
        }
    }

    /**
     * Guard for non-command staff actions (mod-tool). Returns true when the action
     * must be blocked because staff MFA is required but not yet verified this
     * session; in that case it re-shows the popup. Callers should bail out (return)
     * when this returns true.
     */
    public static boolean blockIfLocked(GameClient client) {
        if (client == null || !client.isStaffMfaLocked()) {
            return false;
        }
        sendChallenge(client);
        if (client.getHabbo() != null) {
            client.getHabbo().whisper(
                    Emulator.getTexts().getValue("mfa.staff.locked", "Verifica il codice del tuo authenticator per sbloccare i poteri staff."),
                    com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles.ALERT);
        }
        return true;
    }

    /** otpauth:// provisioning URI for the QR code shown during enrollment. */
    public static String provisioningUri(String account, String secret) {
        return Totp.provisioningUri(issuer(), account, secret);
    }

    /**
     * Sends the Habbo-style MFA popup to the client. If the user is already
     * enrolled we just ask for a code; otherwise we generate/persist a secret and
     * include the otpauth:// URI so the client can render a QR for enrollment.
     */
    public static void sendChallenge(GameClient client) {
        if (client == null || client.getHabbo() == null || client.getHabbo().getHabboInfo() == null) {
            return;
        }
        int userId = client.getHabbo().getHabboInfo().getId();
        if (isEnrolled(userId)) {
            client.sendResponse(new StaffMfaRequiredComposer(true, "", ""));
        } else {
            String secret = getOrCreateSecret(userId);
            String uri = provisioningUri(client.getHabbo().getHabboInfo().getUsername(), secret);
            client.sendResponse(new StaffMfaRequiredComposer(false, secret, uri));
        }
    }

    /**
     * Verify a user-supplied code. On success: marks the row enrolled (first
     * time) and stores the used counter to block immediate replay. Returns true
     * iff the code is valid. The caller is responsible for setting the session's
     * elevated flag and auditing.
     *
     * Hardenings on top of the original implementation:
     *   <ul>
     *     <li>Transactional SELECT…FOR UPDATE + UPDATE so two parallel sockets
     *         submitting the same valid code cannot both elevate (one would have
     *         observed lastCounter == matched and rejected).</li>
     *     <li>Per-account failure rate-limit + temporary lockout, so the 6-digit
     *         code space (≈333k effective entropy with ±1 window) cannot be
     *         brute-forced by scripting many short-lived sockets.</li>
     *   </ul>
     */
    public static boolean verify(int userId, String code) {
        int maxFailures = Math.max(1, Emulator.getConfig().getInt("mfa.staff.lockout.max_failures", 5));
        int windowSec = Math.max(1, Emulator.getConfig().getInt("mfa.staff.lockout.window.seconds", 300));
        int lockoutSec = Math.max(1, Emulator.getConfig().getInt("mfa.staff.lockout.duration.seconds", 900));
        int now = Emulator.getIntUnixTimestamp();

        Connection connection = null;
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            connection.setAutoCommit(false);

            String secret = null;
            long lastCounter = 0;
            int failCount = 0;
            int failWindowStart = 0;
            int lockedUntil = 0;

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT secret, last_counter, fail_count, fail_window_start, locked_until FROM staff_mfa WHERE user_id = ? FOR UPDATE")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        secret = rs.getString("secret");
                        lastCounter = rs.getLong("last_counter");
                        failCount = rs.getInt("fail_count");
                        failWindowStart = rs.getInt("fail_window_start");
                        lockedUntil = rs.getInt("locked_until");
                    }
                }
            }

            if (secret == null || secret.isEmpty()) {
                connection.commit();
                return false;
            }

            // Honour an active lockout regardless of code validity.
            if (lockedUntil > now) {
                connection.commit();
                return false;
            }

            long matched = Totp.verifyAndGetCounter(secret, code);
            // Single-use code (replay protection).
            boolean ok = matched >= 0 && matched > lastCounter;

            if (ok) {
                int affected;
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE staff_mfa SET enrolled = 1, enrolled_at = IF(enrolled_at = 0, ?, enrolled_at), " +
                                "last_counter = ?, last_used_at = ?, fail_count = 0, fail_window_start = 0, locked_until = 0 " +
                                "WHERE user_id = ? AND last_counter < ?")) {
                    ps.setInt(1, now);
                    ps.setLong(2, matched);
                    ps.setInt(3, now);
                    ps.setInt(4, userId);
                    ps.setLong(5, matched);
                    affected = ps.executeUpdate();
                }
                connection.commit();
                // affected == 0 means a parallel verify won the race for this code → treat as fail.
                if (affected == 0) {
                    LOGGER.warn("Riutilizzo concorrente del codice StaffMfa rifiutato per l'utente {} (counter {})", userId, matched);
                    return false;
                }
                return true;
            }

            // Failure path: increment fail counter; rotate window if it expired; lock if we hit the cap.
            int newWindowStart = (failWindowStart == 0 || now - failWindowStart > windowSec) ? now : failWindowStart;
            int newFailCount = (newWindowStart == now) ? 1 : (failCount + 1);
            int newLockedUntil = (newFailCount >= maxFailures) ? (now + lockoutSec) : lockedUntil;

            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE staff_mfa SET fail_count = ?, fail_window_start = ?, locked_until = ? WHERE user_id = ?")) {
                ps.setInt(1, newFailCount);
                ps.setInt(2, newWindowStart);
                ps.setInt(3, newLockedUntil);
                ps.setInt(4, userId);
                ps.executeUpdate();
            }
            connection.commit();

            if (newLockedUntil > now) {
                LOGGER.warn("Utente StaffMfa {} bloccato fino a {} dopo {} fallimenti", userId, newLockedUntil, newFailCount);
                AuditLog.record(userId, "", "STAFF_MFA_LOCKED", "user:" + userId,
                        "fail_count=" + newFailCount + " until=" + newLockedUntil);
            }
            if (matched < 0) {
                return false;
            }
            // matched >= 0 but <= lastCounter: replay attempt.
            LOGGER.warn("Codice StaffMfa replay/in ritardo rifiutato per l'utente {} (counter {} <= {})", userId, matched, lastCounter);
            return false;
        } catch (Exception e) {
            try { if (connection != null) connection.rollback(); } catch (Exception ignored) {}
            LOGGER.error("StaffMfa.verify fallito per l'utente {}", userId, e);
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Genera (e persiste, hashati) un set fresco di N codici di recovery monouso
     * per lo staff MFA. Invalida TUTTI i codici precedenti di questo utente.
     * Ritorna i codici IN CHIARO: il caller li deve mostrare UNA volta sola
     * all'utente, perche' il server da quel momento conosce solo SHA-256.
     * Config: mfa.staff.recovery.count (default 8).
     */
    public static String[] generateAndStoreRecoveryCodes(int userId) {
        int count = Math.max(1, Emulator.getConfig().getInt("mfa.staff.recovery.count", 8));
        String[] codes = new String[count];
        java.security.SecureRandom rng = new java.security.SecureRandom();
        // Alfabeto senza caratteri ambigui (no 0/O/I/1) -> 32 simboli, ~5 bit per char.
        final char[] alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM staff_mfa_recovery_codes WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            int now = Emulator.getIntUnixTimestamp();
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO staff_mfa_recovery_codes (user_id, code_hash, created_at) VALUES (?, ?, ?)")) {
                for (int i = 0; i < count; i++) {
                    String code = generateRecoveryCode(rng, alphabet);
                    codes[i] = code;
                    ps.setInt(1, userId);
                    ps.setString(2, sha256Hex(normalizeRecoveryCode(code)));
                    ps.setInt(3, now);
                    ps.executeUpdate();
                }
            }
            AuditLog.record(userId, "", "STAFF_MFA_RECOVERY_REGENERATED",
                    "user:" + userId, "count=" + count);
            return codes;
        } catch (Exception e) {
            LOGGER.error("StaffMfa.generateAndStoreRecoveryCodes fallito per l'utente {}", userId, e);
            return new String[0];
        }
    }

    /**
     * Verifica e CONSUMA un codice di recovery monouso. UPDATE atomico
     * (WHERE used_at IS NULL) -> due tentativi concorrenti con lo stesso codice
     * non possono entrambi avere successo. Audit-logga STAFF_MFA_RECOVERY_USED.
     * Config: mfa.staff.recovery.enabled (default true).
     */
    public static boolean verifyRecoveryCode(int userId, String inputCode) {
        if (!Emulator.getConfig().getBoolean("mfa.staff.recovery.enabled", true)) return false;
        if (inputCode == null) return false;
        String normalized = normalizeRecoveryCode(inputCode);
        // Difesa: rifiuta input troppo corti per evitare match accidentali coi 6 digit TOTP.
        if (normalized.length() < 8) return false;

        int now = Emulator.getIntUnixTimestamp();
        String hash = sha256Hex(normalized);

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE staff_mfa_recovery_codes SET used_at = ? " +
                             "WHERE user_id = ? AND code_hash = ? AND used_at IS NULL")) {
            ps.setInt(1, now);
            ps.setInt(2, userId);
            ps.setString(3, hash);
            int affected = ps.executeUpdate();
            if (affected == 1) {
                AuditLog.record(userId, "", "STAFF_MFA_RECOVERY_USED", "user:" + userId,
                        "remaining=" + remainingRecoveryCodes(userId));
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("StaffMfa.verifyRecoveryCode fallito per l'utente {}", userId, e);
            return false;
        }
    }

    /** Conta i codici di recovery ancora NON utilizzati per questo utente. */
    public static int remainingRecoveryCodes(int userId) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM staff_mfa_recovery_codes WHERE user_id = ? AND used_at IS NULL")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            LOGGER.error("StaffMfa.remainingRecoveryCodes fallito per l'utente {}", userId, e);
            return 0;
        }
    }

    private static String generateRecoveryCode(java.security.SecureRandom rng, char[] alphabet) {
        // 16 char in 4 gruppi di 4 = ~80 bit di entropia: resistenti a brute-force
        // online anche senza un lockout dedicato sul redeem.
        StringBuilder sb = new StringBuilder(20);
        for (int g = 0; g < 4; g++) {
            if (g > 0) sb.append('-');
            for (int c = 0; c < 4; c++) {
                sb.append(alphabet[rng.nextInt(alphabet.length)]);
            }
        }
        return sb.toString();
    }

    private static String normalizeRecoveryCode(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }

    private static String sha256Hex(String s) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
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
