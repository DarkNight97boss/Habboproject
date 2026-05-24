package com.eu.habbo.networking.gameserver.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/decoders/GameByteFrameDecoder.class */
public class GameByteFrameDecoder extends LengthFieldBasedFrameDecoder {
    private static final int MAX_PACKET_LENGTH = 417792;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int LENGTH_FIELD_ADJUSTMENT = 0;
    private static final int INITIAL_BYTES_TO_STRIP = 4;

    public GameByteFrameDecoder() {
        super(MAX_PACKET_LENGTH, 0, 4, 0, 4);
    }

    protected Object decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        return super.decode(channelHandlerContext, byteBuf);
    }
}
