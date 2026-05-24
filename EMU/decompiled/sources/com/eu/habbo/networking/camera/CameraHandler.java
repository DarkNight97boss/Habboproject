package com.eu.habbo.networking.camera;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/CameraHandler.class */
public class CameraHandler extends ChannelInboundHandlerAdapter {
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object obj) {
        try {
            ByteBuf byteBuf = (ByteBuf) obj;
            ((ByteBuf) obj).readerIndex(0);
            ByteBuf byteBufWrappedBuffer = Unpooled.wrappedBuffer(byteBuf.readBytes(byteBuf.readInt()));
            try {
                CameraPacketHandler.instance().handle(channelHandlerContext.channel(), byteBufWrappedBuffer.readShort(), byteBufWrappedBuffer);
                try {
                    byteBufWrappedBuffer.release();
                } catch (Exception e) {
                }
                try {
                    ((ByteBuf) obj).release();
                } catch (Exception e2) {
                }
            } catch (Exception e3) {
                try {
                    byteBufWrappedBuffer.release();
                } catch (Exception e4) {
                }
                try {
                    ((ByteBuf) obj).release();
                } catch (Exception e5) {
                }
            } catch (Throwable th) {
                try {
                    byteBufWrappedBuffer.release();
                } catch (Exception e6) {
                }
                try {
                    ((ByteBuf) obj).release();
                } catch (Exception e7) {
                }
                throw th;
            }
        } catch (Exception e8) {
            e8.printStackTrace();
        }
    }

    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        CameraClient.attemptReconnect = true;
    }

    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable th) {
        th.printStackTrace();
    }
}
