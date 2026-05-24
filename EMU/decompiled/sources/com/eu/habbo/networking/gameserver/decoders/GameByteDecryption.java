package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.crypto.HabboRC4;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/decoders/GameByteDecryption.class */
public class GameByteDecryption extends ByteToMessageDecoder {
    public GameByteDecryption() {
        setSingleDecode(true);
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        ByteBuf bytes = byteBuf.readBytes(byteBuf.readableBytes());
        ((HabboRC4) channelHandlerContext.channel().attr(GameServerAttributes.CRYPTO_CLIENT).get()).parse(bytes.array());
        list.add(bytes);
    }
}
