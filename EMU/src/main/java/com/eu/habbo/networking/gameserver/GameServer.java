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

                // Cap connessioni per IP (security audit P2.8). In proxy-mode il conteggio
                // parte da HAProxyIpHandler (IP reale); qui in channelActive solo senza proxy.
                // Loopback sempre esente (dietro nginx tutti i client sono 127.0.0.1).
                ch.pipeline().addLast("connLimit", new com.eu.habbo.networking.gameserver.handlers.ConnectionLimitHandler(proxyOn));

                addGameHandlers(ch);
            }
        });
        initializeWebSocketServer();
    }

    /**
     * Catena di handler di gioco condivisa da TCP e WebSocket nativo (decoder, idle,
     * rate-limit, dispatcher, encoder). In un solo posto così i due trasporti non divergono.
     */
    public static void addGameHandlers(SocketChannel ch) {
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
        // Hard reader-side backstop: chiude il channel se NESSUN packet
        // arriva entro N secondi. Defenza vs. scenario in cui il ping/
        // pong loop e' rotto (es. encoder crash) ma il channel resta
        // tecnicamente "open".
        //
        // Default 1800s (30 min): generoso per giocatori AFK in stanza.
        // Il loop ping/pong (45s ping, qualsiasi-packet-resetta) deve
        // mantenerlo sempre rinfrescato; questo timer scatta SOLO se
        // il ping/pong stesso e' broken. Operatore puo' settare 0 per
        // disabilitarlo, oppure abbassare a 600 se vuole essere
        // aggressivo (default storico).
        int readIdle = Emulator.getConfig().getInt("networking.idle.read.seconds", 1800);
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

    // ------------------------------------------------------------------
    // WebSocket nativo (portato da Arcturus Morningstar dev): sostituisce il
    // plugin NitroWebsockets-3.1.jar. OFF di default (ws.enabled=0): il deploy
    // non cambia nulla finché l'operatore non rimuove il plugin e abilita.
    // ------------------------------------------------------------------
    private io.netty.channel.Channel webSocketChannel;

    private void initializeWebSocketServer() {
        if (!Emulator.getConfig().getBoolean("ws.enabled", false)) {
            return;
        }
        String wsHost = Emulator.getConfig().getValue("ws.host", "127.0.0.1");
        int wsPort = Emulator.getConfig().getInt("ws.port", 2096);

        io.netty.bootstrap.ServerBootstrap bootstrap = new io.netty.bootstrap.ServerBootstrap();
        bootstrap.group(this.getBossGroup(), this.getWorkerGroup());
        // Stesso transport del listener TCP (gruppi Epoll ↔ canale Epoll).
        bootstrap.channel(io.netty.channel.epoll.Epoll.isAvailable()
                ? io.netty.channel.epoll.EpollServerSocketChannel.class
                : io.netty.channel.socket.nio.NioServerSocketChannel.class);
        bootstrap.childOption(io.netty.channel.ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(io.netty.channel.ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(io.netty.channel.ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(io.netty.channel.ChannelOption.ALLOCATOR, new io.netty.buffer.UnpooledByteBufAllocator(false));
        bootstrap.childHandler(new WebSocketChannelInitializer());

        io.netty.channel.ChannelFuture f = bootstrap.bind(wsHost, wsPort).awaitUninterruptibly();
        if (!f.isSuccess()) {
            org.slf4j.LoggerFactory.getLogger(GameServer.class).error("WebSocket nativo: bind fallito su {}:{} (porta occupata? plugin NitroWebsockets ancora caricato?)", wsHost, wsPort, f.cause());
        } else {
            this.webSocketChannel = f.channel();
            org.slf4j.LoggerFactory.getLogger(GameServer.class).info("WebSocket nativo in ascolto su {}:{} (header IP: '{}', whitelist: '{}')", wsHost, wsPort,
                    Emulator.getConfig().getValue("ws.ip.header", ""), Emulator.getConfig().getValue("ws.whitelist", "localhost"));
        }
    }

    @Override
    public void stop() {
        if (this.webSocketChannel != null) {
            try { this.webSocketChannel.close().awaitUninterruptibly(); } catch (Exception ignored) { }
            this.webSocketChannel = null;
        }
        super.stop();
    }

    public PacketManager getPacketManager() {
        return this.packetManager;
    }

    public GameClientManager getGameClientManager() {
        return this.gameClientManager;
    }
}
