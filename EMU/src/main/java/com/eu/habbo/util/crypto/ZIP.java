package com.eu.habbo.util.crypto;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

public class ZIP {
    private static final int MAX_INFLATED_OUTPUT = 16 * 1024 * 1024; // 16MB cap (anti zip-bomb / decompression DoS)

    public static byte[] inflate(byte[] data) {
        try {
            byte[] buffer = new byte[65536];
            Inflater inflater = new Inflater();
            inflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(Math.max(64, Math.min(data.length, 1024 * 1024)));
            int total = 0;
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    break;
                }
                total += count;
                if (total > MAX_INFLATED_OUTPUT) { // decompression bomb -> reject
                    inflater.end();
                    return new byte[0];
                }
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            byte[] output = outputStream.toByteArray();

            inflater.end();
            return output;
        } catch (Exception e) {
            return new byte[0];
        }
    }
}