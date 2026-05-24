package com.eu.habbo.messages;

import com.eu.habbo.Emulator;
import com.eu.habbo.util.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/ClientMessage.class */
public class ClientMessage {
    private final int header;
    private final ByteBuf buffer;

    public ClientMessage(int i, ByteBuf byteBuf) {
        this.header = i;
        this.buffer = (byteBuf == null || byteBuf.readableBytes() == 0) ? Unpooled.EMPTY_BUFFER : byteBuf;
    }

    public ByteBuf getBuffer() {
        return this.buffer;
    }

    public int getMessageId() {
        return this.header;
    }

    /* JADX INFO: renamed from: clone, reason: merged with bridge method [inline-methods] */
    public ClientMessage m288clone() throws CloneNotSupportedException {
        return new ClientMessage(this.header, this.buffer.duplicate());
    }

    public int readShort() {
        try {
            return this.buffer.readShort();
        } catch (Exception e) {
            return 0;
        }
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
            byte[] bArr = new byte[readShort()];
            this.buffer.readBytes(bArr);
            return new String(bArr);
        } catch (Exception e) {
            return Emulator.PREVIEW;
        }
    }

    public String getMessageBody() {
        return PacketUtils.formatPacket(this.buffer);
    }

    public int bytesAvailable() {
        return this.buffer.readableBytes();
    }

    public boolean release() {
        return this.buffer.release();
    }
}
