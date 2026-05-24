package com.eu.habbo.networking.gameserver;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClientManager;
import com.eu.habbo.messages.PacketManager;
import com.eu.habbo.networking.Server;
import com.eu.habbo.networking.gameserver.decoders.GameByteDecoder;
import com.eu.habbo.networking.gameserver.decoders.GameByteFrameDecoder;
import com.eu.habbo.networking.gameserver.decoders.GameClientMessageLogger;
import com.eu.habbo.networking.gameserver.decoders.GameMessageHandler;
import com.eu.habbo.networking.gameserver.decoders.GameMessageRateLimit;
import com.eu.habbo.networking.gameserver.decoders.GamePolicyDecoder;
import com.eu.habbo.networking.gameserver.encoders.GameServerMessageEncoder;
import com.eu.habbo.networking.gameserver.encoders.GameServerMessageLogger;
import com.eu.habbo.networking.gameserver.handlers.IdleTimeoutHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/GameServer.class */
public class GameServer extends Server {
    private final PacketManager packetManager;
    private final GameClientManager gameClientManager;

    public GameServer(String str, int i) throws Exception {
        super("Game Server", str, i, Emulator.getConfig().getInt("io.bossgroup.threads"), Emulator.getConfig().getInt("io.workergroup.threads"));
        this.packetManager = new PacketManager();
        this.gameClientManager = new GameClientManager();
    }

    @Override // com.eu.habbo.networking.Server
    public void initializePipeline() {
        super.initializePipeline();
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() { // from class: com.eu.habbo.networking.gameserver.GameServer.1
            public void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast("logger", new LoggingHandler());
                socketChannel.pipeline().addLast(new ChannelHandler[]{new GamePolicyDecoder()});
                socketChannel.pipeline().addLast(new ChannelHandler[]{new GameByteFrameDecoder()});
                socketChannel.pipeline().addLast(new ChannelHandler[]{new GameByteDecoder()});
                if (PacketManager.DEBUG_SHOW_PACKETS) {
                    socketChannel.pipeline().addLast(new ChannelHandler[]{new GameClientMessageLogger()});
                }
                socketChannel.pipeline().addLast("idleEventHandler", new IdleTimeoutHandler(30, 60));
                socketChannel.pipeline().addLast(new ChannelHandler[]{new GameMessageRateLimit()});
                socketChannel.pipeline().addLast(new ChannelHandler[]{new GameMessageHandler()});
                socketChannel.pipeline().addLast(new ChannelHandler[]{new GameServerMessageEncoder()});
                if (PacketManager.DEBUG_SHOW_PACKETS) {
                    socketChannel.pipeline().addLast(new ChannelHandler[]{new GameServerMessageLogger()});
                }
            }
        });
    }

    public PacketManager getPacketManager() {
        return this.packetManager;
    }

    public GameClientManager getGameClientManager() {
        return this.gameClientManager;
    }
}
