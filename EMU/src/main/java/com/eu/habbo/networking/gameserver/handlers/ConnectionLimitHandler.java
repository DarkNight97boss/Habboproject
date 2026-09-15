package com.eu.habbo.networking.gameserver.handlers;

import com.eu.habbo.core.ConnectionLimiter;
import com.eu.habbo.core.HealthEndpoint;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Applica {@link ConnectionLimiter} al game server (security audit, P2.8).
 *
 * Due punti di aggancio, perche' l'IP reale e' noto in momenti diversi:
 *  - senza PROXY protocol: l'IP del socket e' quello del client → conteggio in
 *    channelActive (qui);
 *  - con PROXY protocol: in channelActive l'IP del socket e' quello del proxy
 *    (Cloudflare edge) e contare per quello bloccherebbe interi datacenter →
 *    NON contiamo qui; {@link HAProxyIpHandler} chiama {@link #apply} appena ha
 *    decodificato l'IP reale dall'header PROXY.
 *
 * Il rilascio avviene sempre alla chiusura del canale (closeFuture), una sola
 * volta grazie all'attributo COUNTED_IP.
 */
public class ConnectionLimitHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionLimitHandler.class);
    private static final AttributeKey<String> COUNTED_IP = AttributeKey.valueOf("asteria.connlimit.ip");

    private final boolean proxyMode;

    public ConnectionLimitHandler(boolean proxyMode) {
        this.proxyMode = proxyMode;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!this.proxyMode) {
            String ip = null;
            if (ctx.channel().remoteAddress() instanceof InetSocketAddress) {
                InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
                if (addr.getAddress() != null) ip = addr.getAddress().getHostAddress();
            }
            if (!apply(ctx, ip)) return;
        }
        super.channelActive(ctx);
    }

    /**
     * Conta la connessione per {@code ip}; se oltre il limite chiude il canale.
     * @return true se la connessione puo' proseguire, false se e' stata chiusa.
     */
    public static boolean apply(ChannelHandlerContext ctx, String ip) {
        if (ctx.channel().attr(COUNTED_IP).get() != null) return true; // gia' contata
        ConnectionLimiter limiter = ConnectionLimiter.forGameServer();
        if (!limiter.acquire(ip)) {
            HealthEndpoint.CONNECTIONS_REJECTED.incrementAndGet();
            LOGGER.warn("Connessione rifiutata: {} ha gia' {} connessioni aperte (max {} per IP, network.max.connections.per.ip)",
                    ip, limiter.current(ip), limiter.maxPerIp());
            ctx.close();
            return false;
        }
        if (ConnectionLimiter.isExempt(ip)) return true;
        final String counted = ip;
        ctx.channel().attr(COUNTED_IP).set(counted);
        ctx.channel().closeFuture().addListener(f -> limiter.release(counted));
        return true;
    }
}
