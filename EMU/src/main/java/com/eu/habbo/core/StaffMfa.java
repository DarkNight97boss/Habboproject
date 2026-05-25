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
            LOGGER.error("StaffMfa.isEnrolled failed for user {}", userId, e);
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
            LOGGER.error("StaffMfa.getOrCreateSecret failed for user {}", userId, e);
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
                    Emulator.getTexts().getValue("mfa.staff.locked", "Verify your authenticator code to unlock staff powers."),
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
     */
    public static boolean verify(int userId, String code) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            String secret = null;
            long lastCounter = 0;
            try (PreparedStatement ps = connection.prepareStatement("SELECT secret, last_counter FROM staff_mfa WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        secret = rs.getString("secret");
                        lastCounter = rs.getLong("last_counter");
                    }
                }
            }

            if (secret == null || secret.isEmpty()) {
                return false;
            }

            long matched = Totp.verifyAndGetCounter(secret, code);
            if (matched < 0) {
                return false;
            }

            // Replay protection: a code (counter) can only be consumed once.
            if (matched <= lastCounter) {
                LOGGER.warn("StaffMfa replay/late code rejected for user {} (counter {} <= {})", userId, matched, lastCounter);
                return false;
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE staff_mfa SET enrolled = 1, enrolled_at = IF(enrolled_at = 0, ?, enrolled_at), " +
                            "last_counter = ?, last_used_at = ? WHERE user_id = ?")) {
                int now = Emulator.getIntUnixTimestamp();
                ps.setInt(1, now);
                ps.setLong(2, matched);
                ps.setInt(3, now);
                ps.setInt(4, userId);
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("StaffMfa.verify failed for user {}", userId, e);
            return false;
        }
    }
}
