package com.eu.habbo.networking.gameserver;

import com.eu.habbo.networking.gameserver.codec.WebSocketCodec;
import com.eu.habbo.networking.gameserver.handlers.WebSocketHttpHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * Pipeline del listener WebSocket nativo (sostituisce il plugin NitroWebsockets):
 * HTTP → upgrade WS → frame binari → ByteBuf → la STESSA catena di gioco del
 * listener TCP ({@link GameServer#addGameHandlers}), così rate-limit, idle,
 * frame cap e decoder restano identici su entrambi i trasporti.
 * TLS è terminato da nginx: nessun SSL qui.
 */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    /** Allineato al cap frame dei decoder (417KB) con margine. */
    private static final int MAX_FRAME_SIZE = 512 * 1024;

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast("httpCodec", new HttpServerCodec());
        ch.pipeline().addLast("httpAggregator", new HttpObjectAggregator(MAX_FRAME_SIZE));
        ch.pipeline().addLast("wsHttpHandler", new WebSocketHttpHandler());
        ch.pipeline().addLast("wsProtocolHandler", new WebSocketServerProtocolHandler(WebSocketServerProtocolConfig.newBuilder()
                .websocketPath("/").checkStartsWith(true).allowExtensions(false).maxFramePayloadLength(MAX_FRAME_SIZE).build()));
        ch.pipeline().addLast("wsCodec", new WebSocketCodec());
        GameServer.addGameHandlers(ch);
    }
}
