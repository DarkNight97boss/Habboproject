package com.eu.habbo.util;

import java.util.concurrent.ThreadLocalRandom;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/util/HexUtils.class */
public class HexUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bArr) {
        char[] cArr = new char[bArr.length * 2];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            cArr[i * 2] = HEX_ARRAY[i2 >>> 4];
            cArr[(i * 2) + 1] = HEX_ARRAY[i2 & 15];
        }
        return new String(cArr);
    }

    public static byte[] toBytes(String str) {
        int length = str.length();
        byte[] bArr = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }

    public static String getRandom(int i) {
        ThreadLocalRandom threadLocalRandomCurrent = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < i) {
            sb.append(Integer.toHexString(threadLocalRandomCurrent.nextInt()));
        }
        return sb.toString().substring(0, i);
    }
}
