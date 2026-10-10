package com.eu.habbo.networking.gameserver.handlers;

import com.eu.habbo.Emulator;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Handler della richiesta HTTP di upgrade WebSocket (prima del protocol handler):
 *  - whitelist dell'Origin (ws.whitelist, lista separata da virgola, '*' e '*.dominio' ammessi);
 *  - IP reale del client dall'header ws.ip.header (es. X-Real-IP impostato da nginx
 *    dal CF-Connecting-IP) → attributo WS_IP, letto da Habbo.connect (ip_current)
 *    e da SecureLoginEvent (bind_ip / rate-limit SSO);
 *  - cap connessioni per IP (ConnectionLimitHandler) sull'IP reale, non sul loopback.
 *
 * SICUREZZA: l'header è fidato SOLO perché il listener WS è bindato su 127.0.0.1 e
 * raggiungibile unicamente via nginx, che sovrascrive X-Real-IP. Non esporre ws.port.
 */
public class WebSocketHttpHandler extends ChannelInboundHandlerAdapter {
    private static final String ORIGIN_HEADER = "Origin";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpMessage) {
            if (!handleHttpRequest(ctx, (HttpMessage) msg)) {
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        super.channelRead(ctx, msg);
        ctx.pipeline().remove(this);
    }

    private boolean handleHttpRequest(ChannelHandlerContext ctx, HttpMessage req) {
        String origin = "error";
        try {
            if (req.headers().contains(ORIGIN_HEADER)) {
                origin = getDomainNameFromUrl(req.headers().get(ORIGIN_HEADER));
            }
        } catch (Exception ignored) {
        }

        String whitelist = Emulator.getConfig().getValue("ws.whitelist", "localhost");
        if (!isWhitelisted(origin, whitelist.split(","))) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN,
                    Unpooled.wrappedBuffer("Origin forbidden".getBytes()));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return false;
        }

        String ip = null;
        String ipHeader = Emulator.getConfig().getValue("ws.ip.header", "");
        if (!ipHeader.isEmpty() && req.headers().contains(ipHeader)) {
            ip = req.headers().get(ipHeader).trim();
            int comma = ip.indexOf(','); // X-Forwarded-For può essere una lista: il primo è il client
            if (comma > 0) ip = ip.substring(0, comma).trim();
            if (!ip.isEmpty()) ctx.channel().attr(GameServerAttributes.WS_IP).set(ip);
        }
        if ((ip == null || ip.isEmpty()) && ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
            if (addr.getAddress() != null) ip = addr.getAddress().getHostAddress();
        }
        // Cap per IP reale (loopback esente: senza header vale il comportamento storico).
        return ConnectionLimitHandler.apply(ctx, ip);
    }

    private static String getDomainNameFromUrl(String url) throws Exception {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) throw new IllegalArgumentException("origin senza host");
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    private static boolean isWhitelisted(String toCheck, String[] whitelist) {
        for (String entry : whitelist) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.equals("*")) return true;
            if (trimmed.startsWith("*")) {
                String suffix = trimmed.substring(1);
                if (toCheck.endsWith(suffix) || ("." + toCheck).equals(suffix)) return true;
            } else if (toCheck.equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
    }
}
