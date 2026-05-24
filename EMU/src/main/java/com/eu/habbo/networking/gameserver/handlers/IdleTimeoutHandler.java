package com.eu.habbo.networking.gameserver.handlers;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.outgoing.handshake.PingComposer;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class IdleTimeoutHandler extends ChannelDuplexHandler {
   private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
   private final long pingScheduleNanos;
   private final long pongTimeoutNanos;
   volatile ScheduledFuture<?> pingScheduleFuture;
   volatile long lastPongTime;
   private volatile int state;

   public IdleTimeoutHandler(int pingScheduleSeconds, int pongTimeoutSeconds) {
      this.pingScheduleNanos = Math.max(MIN_TIMEOUT_NANOS, TimeUnit.SECONDS.toNanos(pingScheduleSeconds));
      this.pongTimeoutNanos = Math.max(MIN_TIMEOUT_NANOS, TimeUnit.SECONDS.toNanos(pongTimeoutSeconds));
   }

   private void initialize(ChannelHandlerContext ctx) {
      switch (this.state) {
         case 1:
         case 2:
            return;
         default:
            this.state = 1;
            this.lastPongTime = System.nanoTime();
            if (this.pingScheduleNanos > 0L) {
               this.pingScheduleFuture = ctx.executor().schedule(new IdleTimeoutHandler.PingScheduledTask(ctx), this.pingScheduleNanos, TimeUnit.NANOSECONDS);
            }
      }
   }

   private void destroy() {
      this.state = 2;
      if (this.pingScheduleFuture != null) {
         this.pingScheduleFuture.cancel(false);
         this.pingScheduleFuture = null;
      }
   }

   public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
         this.initialize(ctx);
      }
   }

   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      this.destroy();
   }

   public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
      if (ctx.channel().isActive()) {
         this.initialize(ctx);
      }

      super.channelRegistered(ctx);
   }

   public void channelActive(ChannelHandlerContext ctx) throws Exception {
      this.initialize(ctx);
      super.channelActive(ctx);
   }

   public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      this.destroy();
      super.channelInactive(ctx);
   }

   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (msg instanceof ClientMessage) {
         ClientMessage packet = (ClientMessage)msg;
         if (packet.getMessageId() == 2596) {
            this.lastPongTime = System.nanoTime();
         }
      }

      super.channelRead(ctx, msg);
   }

   private final class PingScheduledTask implements Runnable {
      private final ChannelHandlerContext ctx;

      public PingScheduledTask(ChannelHandlerContext ctx) {
         this.ctx = ctx;
      }

      @Override
      public void run() {
         if (this.ctx.channel().isOpen()) {
            long currentTime = System.nanoTime();
            if (currentTime - IdleTimeoutHandler.this.lastPongTime > IdleTimeoutHandler.this.pongTimeoutNanos) {
               this.ctx.close();
            } else {
               GameClient client = (GameClient)this.ctx.channel().attr(GameServerAttributes.CLIENT).get();
               if (client != null) {
                  client.sendResponse(new PingComposer());
               }

               IdleTimeoutHandler.this.pingScheduleFuture = this.ctx.executor().schedule(this, IdleTimeoutHandler.this.pingScheduleNanos, TimeUnit.NANOSECONDS);
            }
         }
      }
   }
}
