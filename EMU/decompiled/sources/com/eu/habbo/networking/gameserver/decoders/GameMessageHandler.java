package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.PacketManager;
import com.eu.habbo.threading.runnables.ChannelReadHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.ssl.NotSslRecordException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/decoders/GameMessageHandler.class */
@ChannelHandler.Sharable
public class GameMessageHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMessageHandler.class);

    public void channelRegistered(ChannelHandlerContext channelHandlerContext) {
        if (Emulator.getGameServer().getGameClientManager().addClient(channelHandlerContext)) {
            return;
        }
        channelHandlerContext.channel().close();
    }

    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) {
        channelHandlerContext.channel().close();
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
        try {
            ChannelReadHandler channelReadHandler = new ChannelReadHandler(channelHandlerContext, (ClientMessage) obj);
            if (PacketManager.MULTI_THREADED_PACKET_HANDLING) {
                Emulator.getThreading().run(channelReadHandler);
            } else {
                channelReadHandler.run();
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        channelHandlerContext.channel().close();
    }

    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable th) {
        if (th instanceof IOException) {
            channelHandlerContext.channel().close();
            return;
        }
        if (Emulator.getConfig().getBoolean("debug.mode")) {
            if ((th instanceof NotSslRecordException) || (th instanceof DecoderException)) {
                LOGGER.error("Plaintext received instead of ssl, closing channel");
            } else if (th instanceof TooLongFrameException) {
                LOGGER.error("Disconnecting client, reason " + th.getMessage());
            } else if (th instanceof SSLHandshakeException) {
                LOGGER.error("URL Request error from source " + channelHandlerContext.channel().remoteAddress());
            } else if ((th instanceof NoSuchAlgorithmException) || (th instanceof KeyManagementException)) {
                LOGGER.error("Invalid SSL algorithm, only TLSv1.2 supported in the request");
            } else if (th instanceof UnsupportedMessageTypeException) {
                LOGGER.error("There was an illegal SSL request from (X-forwarded-for/CF-Connecting-IP has not being injected yet!) " + channelHandlerContext.channel().remoteAddress());
            } else if (th instanceof SSLException) {
                LOGGER.error("SSL Problem: " + th.getMessage() + th);
            } else {
                LOGGER.error("Disconnecting client, exception in GameMessageHandler.", th);
            }
        }
        channelHandlerContext.channel().close();
    }
}
