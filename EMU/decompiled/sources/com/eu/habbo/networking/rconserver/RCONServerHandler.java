package com.eu.habbo.networking.rconserver;

import com.eu.habbo.Emulator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/rconserver/RCONServerHandler.class */
public class RCONServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RCONServerHandler.class);

    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        String strReplace = channelHandlerContext.channel().remoteAddress().toString().split(":")[0].replace("/", Emulator.PREVIEW);
        Iterator<String> it = Emulator.getRconServer().allowedAdresses.iterator();
        while (it.hasNext()) {
            if (it.next().equalsIgnoreCase(strReplace)) {
                return;
            }
        }
        channelHandlerContext.channel().close();
        LOGGER.warn("RCON Remote connection closed: {}. IP not allowed!", strReplace);
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
        ByteBuf byteBuf = (ByteBuf) obj;
        byte[] bArr = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, bArr);
        String str = new String(bArr);
        Gson gson = new Gson();
        String strHandle = "ERROR";
        String asString = Emulator.PREVIEW;
        try {
            JsonObject jsonObject = (JsonObject) gson.fromJson(str, JsonObject.class);
            asString = jsonObject.get("key").getAsString();
            strHandle = Emulator.getRconServer().handle(channelHandlerContext, asString, jsonObject.get("data").toString());
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error("Unknown RCON Message: {}", asString);
        } catch (Exception e2) {
            LOGGER.error("Invalid RCON Message: {}", str);
            e2.printStackTrace();
        }
        ChannelFuture channelFutureWrite = channelHandlerContext.channel().write(Unpooled.copiedBuffer(strHandle.getBytes()), channelHandlerContext.channel().voidPromise());
        channelHandlerContext.channel().flush();
        channelHandlerContext.flush();
        channelFutureWrite.channel().close();
        byteBuf.release();
    }
}
