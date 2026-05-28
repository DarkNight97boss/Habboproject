package com.eu.habbo.networking;

import com.eu.habbo.Emulator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    /**
     * Epoll is a Linux-only Netty transport that bypasses NIO's selector layer
     * and gives roughly 30-50% more throughput + lower CPU on real game loads.
     * We probe once at class-load and fall back to portable NIO on Windows/macOS
     * so the same JAR runs everywhere.
     */
    private static final boolean USE_EPOLL = Epoll.isAvailable();

    protected final ServerBootstrap serverBootstrap;
    protected final EventLoopGroup bossGroup;
    protected final EventLoopGroup workerGroup;
    private final String name;
    private final String host;
    private final int port;

    public Server(String name, String host, int port, int bossGroupThreads, int workerGroupThreads) throws Exception {
        this.name = name;
        this.host = host;
        this.port = port;

        String threadName = name.replace("Server", "").replace(" ", "");

        if (USE_EPOLL) {
            this.bossGroup = new EpollEventLoopGroup(bossGroupThreads, new DefaultThreadFactory(threadName + "Boss"));
            this.workerGroup = new EpollEventLoopGroup(workerGroupThreads, new DefaultThreadFactory(threadName + "Worker"));
            LOGGER.info("Netty transport for {}: epoll (Linux fast path)", name);
        } else {
            this.bossGroup = new NioEventLoopGroup(bossGroupThreads, new DefaultThreadFactory(threadName + "Boss"));
            this.workerGroup = new NioEventLoopGroup(workerGroupThreads, new DefaultThreadFactory(threadName + "Worker"));
            LOGGER.info("Netty transport for {}: NIO (portable)", name);
        }
        this.serverBootstrap = new ServerBootstrap();
    }

    public void initializePipeline() {
        this.serverBootstrap.group(this.bossGroup, this.workerGroup);
        this.serverBootstrap.channel(USE_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
        this.serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        this.serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        this.serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);

        // Inbound buffer sizing. Il precedente SO_RCVBUF=4096 + FixedRecvByteBufAllocator(4096)
        // forzava una read syscall piccola per ogni pacchetto: ok a 100 utenti, ma a 10k+ diventa
        // un context-switch storm. AdaptiveRecvByteBufAllocator auto-tuna la dimensione del read
        // buffer tra min/max in base al pattern reale -> chat/movement letture piccole, ma un
        // burst camera o inventario drena in una sola syscall.
        int rcvBuf = 32 * 1024;
        int rcvMin = 512, rcvInit = 8 * 1024, rcvMax = 64 * 1024;
        try {
            rcvBuf  = Emulator.getConfig().getInt("networking.so.rcvbuf.bytes", rcvBuf);
            rcvMin  = Emulator.getConfig().getInt("networking.recv.allocator.min", rcvMin);
            rcvInit = Emulator.getConfig().getInt("networking.recv.allocator.initial", rcvInit);
            rcvMax  = Emulator.getConfig().getInt("networking.recv.allocator.max", rcvMax);
        } catch (Throwable ignored) {
            // Config non disponibile su bootstrap precoce -> fallback ai default.
        }
        this.serverBootstrap.childOption(ChannelOption.SO_RCVBUF, rcvBuf);
        this.serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,
                new AdaptiveRecvByteBufAllocator(rcvMin, rcvInit, rcvMax));

        // SO_BACKLOG: profondita' della listen queue del kernel. Default OS ~128 su Linux:
        // a 10k+ utenti, un restart o un login storm la satura e i client ricevono TCP RST
        // prima ancora di completare il TCP handshake.
        int backlog = 4096;
        try {
            backlog = Emulator.getConfig().getInt("networking.so.backlog", backlog);
        } catch (Throwable ignored) {
        }
        this.serverBootstrap.option(ChannelOption.SO_BACKLOG, backlog);

        // SO_REUSEPORT (solo Linux epoll): con bossGroup di N thread, Netty bind-a N listen
        // socket sulla stessa porta e il kernel load-balancia i SYN tra loro. Rimuove il
        // bottleneck "single accept queue" sotto churn alto (login storm, riconnessioni
        // dopo blip di rete). No-op su macOS/Windows o transport NIO.
        if (USE_EPOLL) {
            try {
                this.serverBootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
            } catch (Throwable t) {
                LOGGER.warn("Unable to enable SO_REUSEPORT for {}: {}", this.name, t.toString());
            }
        }
        // Pooled allocator reuses ByteBuf chunks across packets instead of
        // allocating + GCing one per composer write. On a busy room (50
        // users × 30 status broadcasts/sec) that's ~1500 alloc/sec saved
        // and meaningfully lower GC pressure. Pooled mode is heap-backed
        // here (no direct memory) to keep the operational surface unchanged.
        // Operators can opt out via networking.pooled.allocator=false if
        // they hit any pool fragmentation bug in this Netty version.
        boolean usePooled = true;
        try {
            usePooled = Emulator.getConfig().getBoolean("networking.pooled.allocator", true);
        } catch (Throwable ignored) {
        }
        ByteBufAllocator allocator = usePooled
                ? new PooledByteBufAllocator(false)
                : new UnpooledByteBufAllocator(false);
        this.serverBootstrap.childOption(ChannelOption.ALLOCATOR, allocator);

        // Backpressure: cap each channel's outbound buffer. Without this a single
        // slow-loris-on-read client (TCP window 0, never reads) lets the server
        // queue megabytes of broadcasts (room/hotel alerts) and eventually OOMs.
        // When the queue reaches `high`, `isWritable()` flips false; senders check
        // it and either close or drop. The water marks are configurable.
        int low = 256 * 1024;
        int high = 1 << 20;
        try {
            low = Emulator.getConfig().getInt("networking.write.low_watermark.bytes", low);
            high = Emulator.getConfig().getInt("networking.write.high_watermark.bytes", high);
        } catch (Throwable ignored) {
            // Config may not be available yet on early bootstrap; fall back to defaults.
        }
        if (low < 1024) low = 1024;
        if (high <= low) high = low * 4;
        this.serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(low, high));
    }

    public void connect() {
        ChannelFuture channelFuture = this.serverBootstrap.bind(this.host, this.port);

        while (!channelFuture.isDone()) {
        }

        if (!channelFuture.isSuccess()) {
            LOGGER.info("Failed to connect to the host ({}:{})@{}", this.host, this.port, this.name);
            System.exit(0);
        } else {
            LOGGER.info("Started GameServer on {}:{}@{}", this.host, this.port, this.name);
        }
    }

    public void stop() {
        LOGGER.info("Stopping {}", this.name);
        try {
            // Graceful shutdown: lascia che i client in-flight finiscano i
            // pacchetti pending prima di staccare la spina. Cruciale per non
            // perdere trade/purchase committati ma non confermati al client.
            //
            // quietPeriod=2s, timeout=15s:
            //  - durante quietPeriod il gruppo accetta task ancora schedulati
            //    (drain delle write coda)
            //  - se entro timeout non si svuota, hard-stop (no infinite block
            //    su un client malevolo che non legge)
            //
            // Override via Emulator config se serve (default = ragionevole).
            long quiet = 2, deadline = 15;
            try {
                quiet = com.eu.habbo.Emulator.getConfig()
                        .getInt("networking.shutdown.quiet.seconds", 2);
                deadline = com.eu.habbo.Emulator.getConfig()
                        .getInt("networking.shutdown.timeout.seconds", 15);
            } catch (Throwable ignored) {
                // Config non disponibile su shutdown anomalo — usa default.
            }
            this.workerGroup.shutdownGracefully(quiet, deadline, TimeUnit.SECONDS).sync();
            this.bossGroup.shutdownGracefully(quiet, deadline, TimeUnit.SECONDS).sync();
        } catch(InterruptedException e) {
            LOGGER.error("Exception during {} shutdown... HARD STOP", this.name, e);
        }
        LOGGER.info("GameServer Stopped!");
    }

    public ServerBootstrap getServerBootstrap() {
        return this.serverBootstrap;
    }

    public EventLoopGroup getBossGroup() {
        return this.bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return this.workerGroup;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
}