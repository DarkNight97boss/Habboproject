package com.eu.habbo.networking.camera;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/CameraMessage.class */
public class CameraMessage {
    protected final short header;
    protected final ByteBuf buffer = Unpooled.buffer();

    public CameraMessage(short s) {
        this.header = s;
    }

    public short getHeader() {
        return this.header;
    }
}
