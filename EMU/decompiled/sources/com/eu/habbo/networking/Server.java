package com.eu.habbo.networking;

import com.eu.habbo.Emulator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/Server.class */
public abstract class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    protected final ServerBootstrap serverBootstrap;
    protected final EventLoopGroup bossGroup;
    protected final EventLoopGroup workerGroup;
    private final String name;
    private final String host;
    private final int port;

    public Server(String str, String str2, int i, int i2, int i3) throws Exception {
        this.name = str;
        this.host = str2;
        this.port = i;
        String strReplace = str.replace("Server", Emulator.PREVIEW).replace(" ", Emulator.PREVIEW);
        this.bossGroup = new NioEventLoopGroup(i2, new DefaultThreadFactory(strReplace + "Boss"));
        this.workerGroup = new NioEventLoopGroup(i3, new DefaultThreadFactory(strReplace + "Worker"));
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
    }

    public void connect() {
        ChannelFuture channelFutureBind = this.serverBootstrap.bind(this.host, this.port);
        while (!channelFutureBind.isDone()) {
        }
        if (channelFutureBind.isSuccess()) {
            LOGGER.info("Started GameServer on " + this.host + ":" + this.port + "@" + this.name);
        } else {
            LOGGER.info("Failed to connect to the host (" + this.host + ":" + this.port + ")@" + this.name);
            System.exit(0);
        }
    }

    public void stop() {
        LOGGER.info("Stopping " + this.name);
        try {
            this.workerGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS).sync();
            this.bossGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS).sync();
        } catch (InterruptedException e) {
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
