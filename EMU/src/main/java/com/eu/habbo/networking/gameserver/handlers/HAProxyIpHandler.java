package com.eu.habbo.networking.gameserver.handlers;

import com.eu.habbo.Emulator;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * PROXY-protocol (v1/v2) front handler for the game server.
 *
 * Enables putting the raw game port behind an L4 proxy that preserves the real
 * client IP via the PROXY protocol header (e.g. Cloudflare Spectrum, HAProxy,
 * a TCP load balancer). It does two things:
 *
 *   1. Port blinding: only accepts connections whose SOURCE (the proxy) is a
 *      trusted IP (Cloudflare ranges + loopback by default, override with
 *      io.proxy.protocol.trusted). Direct scans/attacks from any other IP are
 *      dropped immediately.
 *   2. Real-IP extraction: reads the PROXY header (decoded by HAProxyMessageDecoder),
 *      stores the real client IP in GameServerAttributes.PROXY_REAL_IP for the
 *      login flow (bans / per-IP logic), then removes itself + the decoder so the
 *      normal pipeline proceeds.
 *
 * Activated only when io.proxy.protocol.enabled = true. Otherwise it is never
 * added to the pipeline and behaviour is unchanged.
 */
public class HAProxyIpHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HAProxyIpHandler.class);

    // Cloudflare published edge ranges (https://www.cloudflare.com/ips/) + loopback.
    private static final String[] DEFAULT_TRUSTED = {
            "127.0.0.1/32", "::1/128",
            "173.245.48.0/20", "103.21.244.0/22", "103.22.200.0/22", "103.31.4.0/22",
            "141.101.64.0/18", "108.162.192.0/18", "190.93.240.0/20", "188.114.96.0/20",
            "197.234.240.0/22", "198.41.128.0/17", "162.158.0.0/15", "104.16.0.0/13",
            "104.24.0.0/14", "172.64.0.0/13", "131.0.72.0/22",
            "2400:cb00::/32", "2606:4700::/32", "2803:f800::/32", "2405:b500::/32",
            "2405:8100::/32", "2a06:98c0::/29", "2c0f:f248::/32"
    };

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String peer = ipOf(ctx);
        if (peer == null || !isTrusted(peer)) {
            LOGGER.warn("Proxy-protocol: rejected connection from untrusted source {}", peer);
            ctx.close();
            return;
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HAProxyMessage) {
            HAProxyMessage proxyMessage = (HAProxyMessage) msg;
            try {
                String realIp = proxyMessage.sourceAddress();
                if (realIp != null && !realIp.isEmpty()) {
                    ctx.channel().attr(GameServerAttributes.PROXY_REAL_IP).set(realIp);
                }
            } finally {
                proxyMessage.release();
            }
            // PROXY header consumed -> drop the proxy handlers and continue normally.
            if (ctx.pipeline().get("haproxyDecoder") != null) {
                ctx.pipeline().remove("haproxyDecoder");
            }
            ctx.pipeline().remove(this);
            return;
        }
        super.channelRead(ctx, msg);
    }

    private static String ipOf(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        }
        return null;
    }

    private static boolean isTrusted(String ip) {
        String configured = Emulator.getConfig().getValue("io.proxy.protocol.trusted", "");
        String[] ranges = (configured != null && !configured.trim().isEmpty()) ? configured.split(";") : DEFAULT_TRUSTED;
        for (String cidr : ranges) {
            if (ipInCidr(ip, cidr.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean ipInCidr(String ip, String cidr) {
        try {
            if (cidr.isEmpty()) return false;
            if (!cidr.contains("/")) return ip.equals(cidr);
            String[] parts = cidr.split("/");
            byte[] ipB = InetAddress.getByName(ip).getAddress();
            byte[] netB = InetAddress.getByName(parts[0]).getAddress();
            if (ipB.length != netB.length) return false; // v4 vs v6
            int bits = Integer.parseInt(parts[1]);
            int fullBytes = bits / 8;
            int rem = bits % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (ipB[i] != netB[i]) return false;
            }
            if (rem > 0) {
                int mask = (0xFF << (8 - rem)) & 0xFF;
                return (ipB[fullBytes] & mask) == (netB[fullBytes] & mask);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
