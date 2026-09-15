package com.eu.habbo.core;

import com.eu.habbo.Emulator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cap di connessioni concorrenti per IP sorgente (security audit, P2.8).
 *
 * Prima non esisteva alcun limite: un singolo IP poteva aprire migliaia di socket
 * (ognuno vivo fino a 20s pre-login) e mettere sotto pressione file descriptor e
 * heap. Il rate-limit dei pacchetti e' per-connessione, quindi non aiuta.
 *
 * Design "sicuro per costruzione":
 *  - contiamo per IP REALE del client: dietro proxy PROXY-protocol (Cloudflare
 *    Spectrum/HAProxy) l'IP viene da {@code GameServerAttributes.PROXY_REAL_IP},
 *    altrimenti dall'indirizzo del socket;
 *  - loopback e IP sconosciuti sono SEMPRE esenti: dietro nginx (websocket
 *    proxy su 127.0.0.1:2096, topologia prod) l'EMU vede ogni giocatore come
 *    127.0.0.1 — contarli insieme bloccherebbe tutti;
 *  - {@code network.max.connections.per.ip} = 0 disattiva del tutto.
 *
 * Classe pura (nessuna dipendenza Netty) cosi' e' testabile in isolamento.
 */
public final class ConnectionLimiter {

    public static final int DEFAULT_MAX_PER_IP = 25;

    private static volatile ConnectionLimiter gameServerInstance;

    private final int maxPerIp;
    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();

    public ConnectionLimiter(int maxPerIp) {
        this.maxPerIp = maxPerIp;
    }

    /** Istanza condivisa del game server, configurata da network.max.connections.per.ip (lazy). */
    public static ConnectionLimiter forGameServer() {
        ConnectionLimiter i = gameServerInstance;
        if (i == null) {
            synchronized (ConnectionLimiter.class) {
                i = gameServerInstance;
                if (i == null) {
                    int max = DEFAULT_MAX_PER_IP;
                    try {
                        max = Emulator.getConfig().getInt("network.max.connections.per.ip", DEFAULT_MAX_PER_IP);
                    } catch (Throwable ignored) {
                        // config non pronta: default prudente
                    }
                    i = gameServerInstance = new ConnectionLimiter(max);
                }
            }
        }
        return i;
    }

    /** Loopback (IPv4, IPv6, IPv4-mapped) e IP vuoti/nulli non vengono mai limitati. */
    public static boolean isExempt(String ip) {
        if (ip == null) return true;
        String t = ip.trim();
        return t.isEmpty()
                || t.equals("127.0.0.1") || t.startsWith("127.")
                || t.equals("::1") || t.equals("0:0:0:0:0:0:0:1")
                || t.startsWith("::ffff:127.");
    }

    /**
     * Registra una nuova connessione per {@code ip}.
     * @return true se ammessa (e contata); false se oltre il limite (NON contata: il
     *         chiamante deve chiudere e NON chiamare release()).
     */
    public boolean acquire(String ip) {
        if (this.maxPerIp <= 0 || isExempt(ip)) return true;
        AtomicInteger c = this.counts.computeIfAbsent(ip, k -> new AtomicInteger());
        int n = c.incrementAndGet();
        if (n > this.maxPerIp) {
            c.decrementAndGet();
            return false;
        }
        return true;
    }

    /** Rilascia una connessione precedentemente ammessa da acquire(). Idempotente sui negativi. */
    public void release(String ip) {
        if (this.maxPerIp <= 0 || isExempt(ip)) return;
        AtomicInteger c = this.counts.get(ip);
        if (c == null) return;
        int n = c.decrementAndGet();
        if (n <= 0) {
            this.counts.remove(ip, c);
        }
    }

    public int current(String ip) {
        AtomicInteger c = ip == null ? null : this.counts.get(ip);
        return c == null ? 0 : Math.max(0, c.get());
    }

    public int maxPerIp() {
        return this.maxPerIp;
    }
}
