package com.eu.habbo.networking.gameserver;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClientManager;
import com.eu.habbo.messages.PacketManager;
import com.eu.habbo.networking.Server;
import com.eu.habbo.networking.gameserver.decoders.*;
import com.eu.habbo.networking.gameserver.encoders.GameServerMessageEncoder;
import com.eu.habbo.networking.gameserver.encoders.GameServerMessageLogger;
import com.eu.habbo.networking.gameserver.handlers.IdleTimeoutHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class GameServer extends Server {
    private final PacketManager packetManager;
    private final GameClientManager gameClientManager;

    public GameServer(String host, int port) throws Exception {
        super("Game Server", host, port, Emulator.getConfig().getInt("io.bossgroup.threads"), Emulator.getConfig().getInt("io.workergroup.threads"));
        this.packetManager = new PacketManager();
        this.gameClientManager = new GameClientManager();
    }

    @Override
    public void initializePipeline() {
        super.initializePipeline();

        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                // PROXY protocol (Cloudflare Spectrum / L4 proxy): must run first to read the header
                // and to drop any connection not coming from a trusted (Cloudflare) source.
                //
                // Storicamente questo blocco aveva due flag config equivalenti
                // (io.proxy.protocol.enabled e networking.tcp.proxy) lette da
                // posti diversi. Settare solo "networking.tcp.proxy=true" (come
                // suggerito in config.ini.example) NON attivava il decoder qui:
                // il pipeline restava nudo, il game server vedeva l'IP del
                // proxy invece dell'IP reale, e Habbo.java cercava il PROXY
                // header in attributi mai popolati. Consolidato in OR logico
                // cosi' BASTA settare una qualsiasi delle due.
                boolean proxyOn = Emulator.getConfig().getBoolean("io.proxy.protocol.enabled")
                        || Emulator.getConfig().getBoolean("networking.tcp.proxy");
                if (proxyOn) {
                    ch.pipeline().addLast("haproxyDecoder", new io.netty.handler.codec.haproxy.HAProxyMessageDecoder());
                    ch.pipeline().addLast("haproxyHandler", new com.eu.habbo.networking.gameserver.handlers.HAProxyIpHandler());
                }

                ch.pipeline().addLast("logger", new LoggingHandler());

                // Decoders.
                ch.pipeline().addLast(new GamePolicyDecoder());
                ch.pipeline().addLast(new GameByteFrameDecoder());
                ch.pipeline().addLast(new GameByteDecoder());

                if (PacketManager.DEBUG_SHOW_PACKETS) {
                    ch.pipeline().addLast(new GameClientMessageLogger());
                }
                // Idle/keepalive: invia ping ogni N secondi, kicka se non si
                // vede ALCUN packet (ora qualsiasi, vedi BUGFIX in
                // IdleTimeoutHandler.channelRead) entro M secondi.
                //
                // Default (45, 180):
                //  - Ping schedule 45s: piu' largo del vecchio 30s, riduce
                //    chatter pingacious con il bridge nitro-websockets.
                //  - Pong/silence timeout 180s: 3 minuti di silenzio TOTALE
                //    prima del kick. Il vecchio 60s causava disconnect "dopo
                //    poco" su client con WS bridge sotto carico o tab in
                //    background.
                // Operatore puo' override via config.ini.
                int pingSec = Emulator.getConfig().getInt("networking.idle.ping.seconds", 45);
                int pongSec = Emulator.getConfig().getInt("networking.idle.pong.seconds", 180);
                ch.pipeline().addLast("idleEventHandler", new IdleTimeoutHandler(pingSec, pongSec));
                // Hard reader-side backstop: if no packet (NOT just pongs) is received
                // for `networking.idle.read.seconds` (default 600s) the channel is closed.
                // The existing IdleTimeoutHandler resets on PongEvent and would never trip
                // against a client sending only synthetic pongs; this catches that case.
                int readIdle = Emulator.getConfig().getInt("networking.idle.read.seconds", 600);
                if (readIdle > 0) {
                    ch.pipeline().addLast("readIdle", new io.netty.handler.timeout.IdleStateHandler(readIdle, 0, 0));
                    ch.pipeline().addLast("readIdleClose", new io.netty.channel.ChannelInboundHandlerAdapter() {
                        @Override
                        public void userEventTriggered(io.netty.channel.ChannelHandlerContext ctx, Object evt) throws Exception {
                            if (evt instanceof io.netty.handler.timeout.IdleStateEvent &&
                                    ((io.netty.handler.timeout.IdleStateEvent) evt).state() == io.netty.handler.timeout.IdleState.READER_IDLE) {
                                ctx.close();
                                return;
                            }
                            super.userEventTriggered(ctx, evt);
                        }
                    });
                }
                ch.pipeline().addLast(new GameMessageRateLimit());
                ch.pipeline().addLast(new GameMessageHandler());

                // Encoders.
                ch.pipeline().addLast(new GameServerMessageEncoder());

                if (PacketManager.DEBUG_SHOW_PACKETS) {
                    ch.pipeline().addLast(new GameServerMessageLogger());
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
