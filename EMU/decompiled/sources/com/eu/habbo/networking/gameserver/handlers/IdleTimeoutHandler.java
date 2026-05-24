package com.eu.habbo.networking.gameserver.handlers;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.outgoing.handshake.PingComposer;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/handlers/IdleTimeoutHandler.class */
public class IdleTimeoutHandler extends ChannelDuplexHandler {
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private final long pingScheduleNanos;
    private final long pongTimeoutNanos;
    volatile ScheduledFuture<?> pingScheduleFuture;
    volatile long lastPongTime;
    private volatile int state;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/handlers/IdleTimeoutHandler$PingScheduledTask.class */
    private final class PingScheduledTask implements Runnable {
        private final ChannelHandlerContext ctx;

        public PingScheduledTask(ChannelHandlerContext channelHandlerContext) {
            this.ctx = channelHandlerContext;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.ctx.channel().isOpen()) {
                if (System.nanoTime() - IdleTimeoutHandler.this.lastPongTime > IdleTimeoutHandler.this.pongTimeoutNanos) {
                    this.ctx.close();
                    return;
                }
                GameClient gameClient = (GameClient) this.ctx.channel().attr(GameServerAttributes.CLIENT).get();
                if (gameClient != null) {
                    gameClient.sendResponse(new PingComposer());
                }
                IdleTimeoutHandler.this.pingScheduleFuture = this.ctx.executor().schedule(this, IdleTimeoutHandler.this.pingScheduleNanos, TimeUnit.NANOSECONDS);
            }
        }
    }

    public IdleTimeoutHandler(int i, int i2) {
        this.pingScheduleNanos = Math.max(MIN_TIMEOUT_NANOS, TimeUnit.SECONDS.toNanos(i));
        this.pongTimeoutNanos = Math.max(MIN_TIMEOUT_NANOS, TimeUnit.SECONDS.toNanos(i2));
    }

    private void initialize(ChannelHandlerContext channelHandlerContext) {
        switch (this.state) {
            case 1:
            case 2:
                break;
            default:
                this.state = 1;
                this.lastPongTime = System.nanoTime();
                if (this.pingScheduleNanos > 0) {
                    this.pingScheduleFuture = channelHandlerContext.executor().schedule(new PingScheduledTask(channelHandlerContext), this.pingScheduleNanos, TimeUnit.NANOSECONDS);
                }
                break;
        }
    }

    private void destroy() {
        this.state = 2;
        if (this.pingScheduleFuture != null) {
            this.pingScheduleFuture.cancel(false);
            this.pingScheduleFuture = null;
        }
    }

    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        if (channelHandlerContext.channel().isActive() && channelHandlerContext.channel().isRegistered()) {
            initialize(channelHandlerContext);
        }
    }

    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
        destroy();
    }

    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        if (channelHandlerContext.channel().isActive()) {
            initialize(channelHandlerContext);
        }
        super.channelRegistered(channelHandlerContext);
    }

    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        initialize(channelHandlerContext);
        super.channelActive(channelHandlerContext);
    }

    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        destroy();
        super.channelInactive(channelHandlerContext);
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
        if ((obj instanceof ClientMessage) && ((ClientMessage) obj).getMessageId() == 2596) {
            this.lastPongTime = System.nanoTime();
        }
        super.channelRead(channelHandlerContext, obj);
    }
}
