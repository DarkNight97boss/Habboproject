package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.haproxy.HAProxyMessage;

/**
 * Captures the real client IP from the PROXY protocol header (sent by a trusted TCP proxy such as
 * Cloudflare Spectrum) and stores it on the channel so login/ban/anti-flood logic can use it.
 * Only installed when {@code networking.tcp.proxy=true}.
 */
public class HAProxyIpHandler extends SimpleChannelInboundHandler<HAProxyMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HAProxyMessage msg) {
        if (msg.sourceAddress() != null && !msg.sourceAddress().isEmpty()) {
            ctx.channel().attr(GameServerAttributes.IP).set(msg.sourceAddress());
        }

        // The PROXY header is sent once at the start of the connection; remove ourselves afterwards.
        ctx.pipeline().remove(this);
    }
}
