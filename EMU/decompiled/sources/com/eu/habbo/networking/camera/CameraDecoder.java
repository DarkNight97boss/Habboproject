package com.eu.habbo.networking.camera;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/camera/CameraDecoder.class */
class CameraDecoder extends ByteToMessageDecoder {
    CameraDecoder() {
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int i = byteBuf.readerIndex();
        if (byteBuf.readableBytes() < 6) {
            byteBuf.readerIndex(i);
            return;
        }
        int i2 = byteBuf.readInt();
        byteBuf.readerIndex(i);
        if (byteBuf.readableBytes() < i2) {
            byteBuf.readerIndex(i);
        } else {
            byteBuf.readerIndex(i);
            list.add(byteBuf.readBytes(i2 + 4));
        }
    }
}
