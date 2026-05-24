package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.ChannelHandlerContext;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/ChannelReadHandler.class */
public class ChannelReadHandler implements Runnable {
    private final ChannelHandlerContext ctx;
    private final ClientMessage message;

    public ChannelReadHandler(ChannelHandlerContext channelHandlerContext, ClientMessage clientMessage) {
        this.ctx = channelHandlerContext;
        this.message = clientMessage;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            GameClient gameClient = (GameClient) this.ctx.channel().attr(GameServerAttributes.CLIENT).get();
            if (gameClient != null) {
                Emulator.getGameServer().getPacketManager().handlePacket(gameClient, this.message);
            }
        } finally {
            this.message.release();
        }
    }
}
