package com.eu.habbo.messages;

import com.eu.habbo.util.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClientMessage {
    private final int header;
    private final ByteBuf buffer;

    public ClientMessage(int messageId, ByteBuf buffer) {
        this.header = messageId;
        this.buffer = ((buffer == null) || (buffer.readableBytes() == 0) ? Unpooled.EMPTY_BUFFER : buffer);
    }

    public ByteBuf getBuffer() {
        return this.buffer;
    }

    public int getMessageId() {
        return this.header;
    }
    
    
    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public ClientMessage clone() throws CloneNotSupportedException {
        return new ClientMessage(this.header, this.buffer.duplicate());
    }

    public int readShort() {
        try {
            return this.buffer.readShort();
        } catch (Exception e) {
        }

        return 0;
    }

    public Integer readInt() {
        try {
            return this.buffer.readInt();
        } catch (Exception e) {
        }

        return 0;
    }

    public boolean readBoolean() {
        try {
            return this.buffer.readByte() == 1;
        } catch (Exception e) {
        }

        return false;
    }

    public String readString() {
        // The no-arg variant keeps the historical contract (bounded only by
        // the packet's remaining bytes). Prefer readString(int) at every call
        // site where the field semantics imply an upper bound — handler-level
        // bounding is the only place we know the real max.
        return readString(Integer.MAX_VALUE);
    }

    /**
     * Read a UTF-8 string with an explicit upper bound on the *byte* length.
     *
     * Bounding strings at the parser is the cheapest place to defeat memory
     * amplification: a malicious client can otherwise frame a 64KB payload
     * containing a single string field and force the server to allocate +
     * intern that string in every code path the handler touches. By the time
     * a handler-level length-check runs we've already eaten the allocation.
     *
     * Behaviour when the wire length exceeds maxBytes:
     *   - we still consume the bytes (so the packet stays framed)
     *   - we return the first maxBytes decoded as UTF-8
     *   - we DO NOT throw — handlers that need to reject oversize input
     *     should compare the returned string length to maxBytes
     *
     * Pass {@link Integer#MAX_VALUE} to opt out (legacy behaviour).
     */
    public String readString(int maxBytes) {
        try {
            int length = this.readShort();
            if (length <= 0) return "";
            // Clamp the allocation to what's actually available (anti over-allocation/DoS).
            length = Math.min(length, this.buffer.readableBytes());
            if (length <= 0) return "";
            int take = Math.min(length, Math.max(0, maxBytes));
            byte[] data = new byte[take];
            this.buffer.readBytes(data);
            // Discard the overflow so the next field still parses correctly.
            int discard = length - take;
            if (discard > 0) {
                this.buffer.skipBytes(discard);
            }
            return new String(data);
        } catch (Exception e) {
            return "";
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