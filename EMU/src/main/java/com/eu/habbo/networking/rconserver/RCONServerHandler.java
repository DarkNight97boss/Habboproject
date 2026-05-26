package com.eu.habbo.networking.rconserver;


import com.eu.habbo.Emulator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class RCONServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RCONServerHandler.class);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String adress = ctx.channel().remoteAddress().toString().split(":")[0].replace("/", "");

        for (String s : Emulator.getRconServer().allowedAdresses) {
            if (s.equalsIgnoreCase(adress)) {
                return;
            }
        }

        ctx.channel().close();

        LOGGER.warn("RCON Remote connection closed: {}. IP not allowed!", adress);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf data = (ByteBuf) msg;

        byte[] d = new byte[data.readableBytes()];
        data.getBytes(0, d);
        String message = new String(d);
        Gson gson = new Gson();
        String response = "ERROR";
        String key = "";
        try {
            JsonObject object = gson.fromJson(message, JsonObject.class);

            // Token authentication: defense-in-depth on top of the IP allowlist.
            // Enforced only when rcon.token is configured (non-empty) to stay backward compatible.
            String requiredToken = Emulator.getConfig().getValue("rcon.token", "");
            boolean authorized = (requiredToken == null || requiredToken.isEmpty());
            if (!authorized) {
                String providedToken = (object.has("token") && !object.get("token").isJsonNull())
                        ? object.get("token").getAsString() : "";
                // Constant-time compare so an attacker cannot leak the token
                // byte-by-byte from response timing (mostly mitigated by the IP
                // allowlist, but if `rcon.allowed` ever widens — k8s pod range,
                // CMS box — this is the line of defence).
                authorized = MessageDigest.isEqual(
                        requiredToken.getBytes(StandardCharsets.UTF_8),
                        providedToken.getBytes(StandardCharsets.UTF_8));
            }

            if (!authorized) {
                LOGGER.warn("RCON request rejected (invalid token) from {}", ctx.channel().remoteAddress());
                response = "ERROR";
            } else {
                key = object.get("key").getAsString();
                String dataStr = object.has("data") ? object.get("data").toString() : "";
                response = Emulator.getRconServer().handle(ctx, key, dataStr);
                com.eu.habbo.core.AuditLog.record(0, "RCON", key, "", dataStr); // tamper-evident audit trail
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error("Unknown RCON Message: {}", key);
        } catch (Exception e) {
            LOGGER.error("Invalid RCON Message: {}", message);
            e.printStackTrace();
        }

        ChannelFuture f = ctx.channel().write(Unpooled.copiedBuffer(response.getBytes()), ctx.channel().voidPromise());
        ctx.channel().flush();
        ctx.flush();
        f.channel().close();
        data.release();
    }
}
