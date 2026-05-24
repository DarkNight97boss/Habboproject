package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.messages.ClientMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/decoders/GameByteDecoder.class */
public class GameByteDecoder extends ByteToMessageDecoder {
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        list.add(new ClientMessage(byteBuf.readShort(), Unpooled.copiedBuffer(byteBuf.readBytes(byteBuf.readableBytes()))));
    }
}
