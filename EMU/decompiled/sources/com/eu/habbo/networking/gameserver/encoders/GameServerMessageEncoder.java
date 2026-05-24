package com.eu.habbo.networking.gameserver.encoders;

import com.eu.habbo.messages.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.IllegalReferenceCountException;
import java.io.IOException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/encoders/GameServerMessageEncoder.class */
public class GameServerMessageEncoder extends MessageToByteEncoder<ServerMessage> {
    /* JADX INFO: Access modifiers changed from: protected */
    public void encode(ChannelHandlerContext channelHandlerContext, ServerMessage serverMessage, ByteBuf byteBuf) throws Exception {
        try {
            ByteBuf byteBuf2 = serverMessage.get();
            try {
                byteBuf.writeBytes(byteBuf2);
                byteBuf2.release();
            } catch (Throwable th) {
                byteBuf2.release();
                throw th;
            }
        } catch (IllegalReferenceCountException e) {
            throw new IOException(String.format("IllegalReferenceCountException happened for ServerMessage with packet id %d.", Integer.valueOf(serverMessage.getHeader())), e);
        }
    }
}
