package com.eu.habbo.networking.camera;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.Channel;
import java.io.IOException;
import java.nio.charset.Charset;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/CameraOutgoingMessage.class */
public abstract class CameraOutgoingMessage extends CameraMessage {
    private final ByteBufOutputStream stream;

    public CameraOutgoingMessage(short s) {
        super(s);
        this.stream = new ByteBufOutputStream(this.buffer);
        try {
            this.stream.writeInt(0);
            this.stream.writeShort(s);
        } catch (Exception e) {
        }
    }

    public void appendRawBytes(byte[] bArr) {
        try {
            this.stream.write(bArr);
        } catch (IOException e) {
        }
    }

    public void appendString(String str) {
        try {
            byte[] bytes = str.getBytes();
            this.stream.writeInt(bytes.length);
            this.stream.write(bytes);
        } catch (IOException e) {
        }
    }

    public void appendChar(int i) {
        try {
            this.stream.writeChar(i);
        } catch (IOException e) {
        }
    }

    public void appendChars(Object obj) {
        try {
            this.stream.writeChars(obj.toString());
        } catch (IOException e) {
        }
    }

    public void appendInt32(Integer num) {
        try {
            this.stream.writeInt(num.intValue());
        } catch (IOException e) {
        }
    }

    public void appendInt32(Byte b) {
        try {
            this.stream.writeInt(b.byteValue());
        } catch (IOException e) {
        }
    }

    public void appendInt32(Boolean bool) {
        try {
            this.stream.writeInt(bool.booleanValue() ? 1 : 0);
        } catch (IOException e) {
        }
    }

    public void appendShort(int i) {
        try {
            this.stream.writeShort((short) i);
        } catch (IOException e) {
        }
    }

    public void appendByte(Integer num) {
        try {
            this.stream.writeByte(num.intValue());
        } catch (IOException e) {
        }
    }

    public void appendBoolean(Boolean bool) {
        try {
            this.stream.writeBoolean(bool.booleanValue());
        } catch (IOException e) {
        }
    }

    public CameraOutgoingMessage appendResponse(CameraOutgoingMessage cameraOutgoingMessage) {
        try {
            this.stream.write(cameraOutgoingMessage.get().array());
        } catch (IOException e) {
        }
        return this;
    }

    public String getBodyString() {
        ByteBuf byteBufDuplicate = this.stream.buffer().duplicate();
        byteBufDuplicate.setInt(0, byteBufDuplicate.writerIndex() - 4);
        String string = byteBufDuplicate.toString(Charset.forName("UTF-8"));
        for (int i = 0; i < 14; i++) {
            string = string.replace(Character.toString((char) i), "[" + i + "]");
        }
        byteBufDuplicate.discardSomeReadBytes();
        return string;
    }

    public ByteBuf get() {
        this.buffer.setInt(0, this.buffer.writerIndex() - 4);
        return this.buffer.copy();
    }

    public abstract void compose(Channel channel);
}
