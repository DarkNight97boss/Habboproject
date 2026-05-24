package com.eu.habbo.messages;

import com.eu.habbo.Emulator;
import com.eu.habbo.util.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/ServerMessage.class */
public class ServerMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerMessage.class);
    private boolean initialized;
    private int header;
    private AtomicInteger refs;
    private ByteBufOutputStream stream;
    private ByteBuf channelBuffer;

    public ServerMessage() {
    }

    public ServerMessage(int i) {
        init(i);
    }

    public ServerMessage init(int i) {
        if (this.initialized) {
            throw new ServerMessageException("ServerMessage was already initialized.");
        }
        this.initialized = true;
        this.header = i;
        this.refs = new AtomicInteger(0);
        this.channelBuffer = Unpooled.buffer();
        this.stream = new ByteBufOutputStream(this.channelBuffer);
        try {
            this.stream.writeInt(0);
            this.stream.writeShort(i);
            return this;
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendRawBytes(byte[] bArr) {
        try {
            this.stream.write(bArr);
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendString(String str) {
        if (str == null) {
            appendString(Emulator.PREVIEW);
            return;
        }
        try {
            byte[] bytes = str.getBytes();
            this.stream.writeShort(bytes.length);
            this.stream.write(bytes);
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendChar(int i) {
        try {
            this.stream.writeChar(i);
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendChars(Object obj) {
        try {
            this.stream.writeChars(obj.toString());
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendInt(Integer num) {
        try {
            this.stream.writeInt(num.intValue());
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendInt(Short sh) {
        appendShort(0);
        appendShort(sh.shortValue());
    }

    public void appendInt(Byte b) {
        try {
            this.stream.writeInt(b.byteValue());
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendInt(Boolean bool) {
        try {
            this.stream.writeInt(bool.booleanValue() ? 1 : 0);
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendShort(int i) {
        try {
            this.stream.writeShort((short) i);
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendByte(Integer num) {
        try {
            this.stream.writeByte(num.intValue());
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendBoolean(Boolean bool) {
        try {
            this.stream.writeBoolean(bool.booleanValue());
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendDouble(double d) {
        try {
            this.stream.writeDouble(d);
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void appendDouble(Double d) {
        try {
            this.stream.writeDouble(d.doubleValue());
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public ServerMessage appendResponse(ServerMessage serverMessage) {
        try {
            this.stream.write(serverMessage.get().array());
            return this;
        } catch (IOException e) {
            throw new ServerMessageException(e);
        }
    }

    public void append(ISerialize iSerialize) {
        iSerialize.serialize(this);
    }

    public String getBodyString() {
        return PacketUtils.formatPacket(this.channelBuffer);
    }

    public int getHeader() {
        return this.header;
    }

    public ByteBuf get() {
        this.channelBuffer.setInt(0, this.channelBuffer.writerIndex() - 4);
        return this.channelBuffer.copy();
    }
}
