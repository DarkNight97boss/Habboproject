package com.eu.habbo.networking.gameserver.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/decoders/GamePolicyDecoder.class */
public class GamePolicyDecoder extends ByteToMessageDecoder {
    private static final String POLICY = "<?xml version=\"1.0\"?>\n  <!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">\n  <cross-domain-policy>\n  <allow-access-from domain=\"*\" to-ports=\"1-31111\" />\n  </cross-domain-policy>\u0000";

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byteBuf.markReaderIndex();
        if (byteBuf.readByte() == 60) {
            byteBuf.resetReaderIndex();
            channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(POLICY, CharsetUtil.UTF_8)).addListener(ChannelFutureListener.CLOSE);
        } else {
            channelHandlerContext.pipeline().remove(this);
            byteBuf.resetReaderIndex();
        }
    }
}
