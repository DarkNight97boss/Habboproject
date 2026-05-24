package com.eu.habbo.util.crypto;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/util/crypto/ZIP.class */
public class ZIP {
    public static byte[] inflate(byte[] bArr) {
        try {
            byte[] bArr2 = new byte[bArr.length * 5];
            Inflater inflater = new Inflater();
            inflater.setInput(bArr);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
            while (!inflater.finished()) {
                byteArrayOutputStream.write(bArr2, 0, inflater.inflate(bArr2));
            }
            byteArrayOutputStream.close();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            inflater.end();
            return byteArray;
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
