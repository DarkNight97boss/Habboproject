package com.eu.habbo.crypto.utils;

import java.math.BigInteger;
import java.util.Arrays;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/crypto/utils/BigIntegerUtils.class */
public class BigIntegerUtils {
    public static byte[] toUnsignedByteArray(BigInteger bigInteger) {
        byte[] byteArray = bigInteger.toByteArray();
        if (byteArray[0] == 0) {
            byteArray = Arrays.copyOfRange(byteArray, 1, byteArray.length);
        }
        return byteArray;
    }
}
