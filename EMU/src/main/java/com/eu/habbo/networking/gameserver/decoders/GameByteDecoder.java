package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.messages.ClientMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class GameByteDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 2) { return; } // guard against malformed/short frame (anti-crash)
        short header = in.readShort();
        // NOTE: a readRetainedSlice() would halve GC pressure here, but the downstream
        // handler chain does NOT release the ClientMessage body buffer — switching
        // would silently pin frame buffers forever. Keep the safe copy until a
        // holistic release-on-finish refactor is done.
        ByteBuf body = Unpooled.copiedBuffer(in.readBytes(in.readableBytes()));

        out.add(new ClientMessage(header, body));
    }
}