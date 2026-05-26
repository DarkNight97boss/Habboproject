package com.eu.habbo.networking;

import com.eu.habbo.Emulator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

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

        this.bossGroup = new NioEventLoopGroup(bossGroupThreads, new DefaultThreadFactory(threadName + "Boss"));
        this.workerGroup = new NioEventLoopGroup(workerGroupThreads, new DefaultThreadFactory(threadName + "Worker"));
        this.serverBootstrap = new ServerBootstrap();
    }

    public void initializePipeline() {
        this.serverBootstrap.group(this.bossGroup, this.workerGroup);
        this.serverBootstrap.channel(NioServerSocketChannel.class);
        this.serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        this.serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        this.serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        this.serverBootstrap.childOption(ChannelOption.SO_RCVBUF, 4096);
        this.serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(4096));
        this.serverBootstrap.childOption(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false));

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
            this.workerGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
            this.bossGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
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