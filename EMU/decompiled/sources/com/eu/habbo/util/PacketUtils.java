package com.eu.habbo.util;

import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/util/PacketUtils.class */
public class PacketUtils {
    public static String formatPacket(ByteBuf byteBuf) {
        String string = byteBuf.toString(Charset.defaultCharset());
        for (int i = -1; i < 31; i++) {
            string = string.replace(Character.toString((char) i), "[" + i + "]");
        }
        return string;
    }
}
