package com.eu.habbo.networking.camera;

import com.eu.habbo.Emulator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.nio.charset.Charset;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/CameraIncomingMessage.class */
public abstract class CameraIncomingMessage extends CameraMessage {
    public CameraIncomingMessage(Short sh, ByteBuf byteBuf) {
        super(sh.shortValue());
        this.buffer.writerIndex(0).writeBytes(byteBuf);
    }

    public int readShort() {
        return this.buffer.readShort();
    }

    public Integer readInt() {
        try {
            return Integer.valueOf(this.buffer.readInt());
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean readBoolean() {
        try {
            return this.buffer.readByte() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public String readString() {
        try {
            byte[] bArr = new byte[readInt().intValue()];
            this.buffer.readBytes(bArr);
            return new String(bArr);
        } catch (Exception e) {
            return Emulator.PREVIEW;
        }
    }

    public String getMessageBody() {
        String string = this.buffer.toString(Charset.defaultCharset());
        for (int i = -1; i < 31; i++) {
            string = string.replace(Character.toString((char) i), "[" + i + "]");
        }
        return string;
    }

    public int bytesAvailable() {
        return this.buffer.readableBytes();
    }

    public abstract void handle(Channel channel) throws Exception;
}
