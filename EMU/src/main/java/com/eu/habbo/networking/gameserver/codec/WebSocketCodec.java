package com.eu.habbo.networking.gameserver.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

/**
 * Ponte WebSocket ↔ ByteBuf: ogni frame binario in ingresso diventa il ByteBuf
 * che la normale catena di decoder Habbo si aspetta; ogni ByteBuf in uscita
 * viene incapsulato in un frame binario. (Portato da Arcturus Morningstar dev,
 * sostituisce il plugin NitroWebsockets.)
 */
public class WebSocketCodec extends MessageToMessageCodec<WebSocketFrame, ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        out.add(new BinaryWebSocketFrame(in).retain());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame in, List<Object> out) {
        out.add(in.content().retain());
    }
}
