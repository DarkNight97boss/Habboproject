package com.eu.habbo.networking.camera;

import com.eu.habbo.networking.camera.messages.outgoing.CameraLoginComposer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/CameraClient.class */
public class CameraClient {
    private static final String host = "google.com";
    private static final int port = 1232;
    public static ChannelFuture channelFuture;
    private static Channel channel;
    private final Bootstrap bootstrap = new Bootstrap();
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraClient.class);
    public static boolean isLoggedIn = false;
    public static boolean attemptReconnect = true;

    public CameraClient() {
        this.bootstrap.group(new NioEventLoopGroup());
        this.bootstrap.channel(NioSocketChannel.class);
        this.bootstrap.option(ChannelOption.TCP_NODELAY, true);
        this.bootstrap.option(ChannelOption.SO_KEEPALIVE, false);
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() { // from class: com.eu.habbo.networking.camera.CameraClient.1
            public void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new ChannelHandler[]{new CameraDecoder()});
                socketChannel.pipeline().addLast(new ChannelHandler[]{new CameraHandler()});
            }
        });
        this.bootstrap.option(ChannelOption.SO_RCVBUF, 5120);
        this.bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        this.bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(5120));
        this.bootstrap.option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false));
    }

    public void connect() {
        channelFuture = this.bootstrap.connect(host, port);
        while (!channelFuture.isDone()) {
        }
        if (channelFuture.isSuccess()) {
            attemptReconnect = false;
            channel = channelFuture.channel();
            LOGGER.info("Connected to the Camera Server. Attempting to login.");
            sendMessage(new CameraLoginComposer());
            return;
        }
        LOGGER.error("Failed to connect to the Camera Server. Server unreachable.");
        channel = null;
        channelFuture.channel().close();
        channelFuture = null;
        attemptReconnect = true;
    }

    public void disconnect() {
        if (channelFuture != null) {
            try {
                channelFuture.channel().close().sync();
                channelFuture = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        channel = null;
        isLoggedIn = false;
        LOGGER.info("Disconnected from the camera server.");
    }

    public void sendMessage(CameraOutgoingMessage cameraOutgoingMessage) {
        try {
            if (isLoggedIn || (cameraOutgoingMessage instanceof CameraLoginComposer)) {
                cameraOutgoingMessage.compose(channel);
                channel.write(cameraOutgoingMessage.get().copy(), channel.voidPromise());
                channel.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
