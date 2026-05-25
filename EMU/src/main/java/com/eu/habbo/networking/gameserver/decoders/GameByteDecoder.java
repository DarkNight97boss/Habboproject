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
        ByteBuf body = Unpooled.copiedBuffer(in.readBytes(in.readableBytes()));

        out.add(new ClientMessage(header, body));
    }
}