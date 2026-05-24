package com.eu.habbo.networking.gameserver.encoders;

import com.eu.habbo.crypto.HabboRC4;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/encoders/GameByteEncryption.class */
public class GameByteEncryption extends ChannelOutboundHandlerAdapter {
    public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
        ByteBuf byteBuf = (ByteBuf) obj;
        ByteBuf bytes = byteBuf.readBytes(byteBuf.readableBytes());
        ReferenceCountUtil.release(byteBuf);
        ((HabboRC4) channelHandlerContext.channel().attr(GameServerAttributes.CRYPTO_SERVER).get()).parse(bytes.array());
        channelHandlerContext.write(bytes, channelPromise);
    }
}
