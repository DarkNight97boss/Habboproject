package com.eu.habbo.core;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * RFC-6238 TOTP (Time-based One-Time Password), compatible with Google Authenticator
 * (SHA-1, 30s step, 6 digits, Base32 secret). Used for staff step-up MFA.
 */
public final class Totp {

    private static final int DIGITS = 6;
    private static final int STEP_SECONDS = 30;
    private static final int WINDOW = 1; // accept current +/- 1 step (clock skew tolerance)
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Totp() {
    }

    /** Generate a new random Base32 secret (160-bit) for enrollment. */
    public static String generateSecret() {
        byte[] bytes = new byte[20];
        RANDOM.nextBytes(bytes);
        return base32Encode(bytes);
    }

    /** otpauth:// URI for QR enrollment in Google Authenticator. */
    public static String provisioningUri(String issuer, String account, String base32Secret) {
        String label = urlEncode(issuer) + ":" + urlEncode(account);
        return "otpauth://totp/" + label + "?secret=" + base32Secret + "&issuer=" + urlEncode(issuer) + "&digits=" + DIGITS + "&period=" + STEP_SECONDS;
    }

    /** Validate a user-supplied 6-digit code against the secret, with +/- WINDOW tolerance. */
    public static boolean verify(String base32Secret, String code) {
        return verifyAndGetCounter(base32Secret, code) >= 0;
    }

    /**
     * Like {@link #verify} but returns the matched time-step counter (>= 0) so the
     * caller can enforce single-use (replay) protection, or -1 if the code is invalid.
     */
    public static long verifyAndGetCounter(String base32Secret, String code) {
        if (base32Secret == null || code == null) return -1;
        code = code.trim().replace(" ", "");
        if (!code.matches("\\d{" + DIGITS + "}")) return -1;
        byte[] key = base32Decode(base32Secret);
        if (key.length == 0) return -1;
        long counter = System.currentTimeMillis() / 1000L / STEP_SECONDS;
        for (int w = -WINDOW; w <= WINDOW; w++) {
            String candidate = generate(key, counter + w);
            if (constantTimeEquals(candidate, code)) {
                return counter + w;
            }
        }
        return -1;
    }

    private static String generate(byte[] key, long counter) {
        try {
            byte[] data = new byte[8];
            long c = counter;
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (c & 0xFF);
                c >>= 8;
            }
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                sb.append(BASE32.charAt((buffer >> bits) & 0x1F));
            }
        }
        if (bits > 0) {
            sb.append(BASE32.charAt((buffer << (5 - bits)) & 0x1F));
        }
        return sb.toString();
    }

    private static byte[] base32Decode(String s) {
        try {
            s = s.trim().replace("=", "").toUpperCase();
            int buffer = 0, bits = 0;
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            for (char c : s.toCharArray()) {
                int val = BASE32.indexOf(c);
                if (val < 0) continue;
                buffer = (buffer << 5) | val;
                bits += 5;
                if (bits >= 8) {
                    bits -= 8;
                    out.write((buffer >> bits) & 0xFF);
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }
}
