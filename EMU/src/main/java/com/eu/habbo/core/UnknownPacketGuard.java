package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-Tanji / anti-G-Earth header fuzzing detection.
 *
 * Tanji-class packet sniffers/injectors are typically used in two modes:
 *  - replay/modify of legitimate packets (low signal here, server validation
 *    catches it via the existing rate-limit + ownership checks);
 *  - "fuzzing": the operator manually sends arbitrary 2-byte header ids to
 *    probe the server for hidden / unguarded handlers.
 *
 * A legitimate nitro client only emits headers that {@link
 * com.eu.habbo.messages.PacketManager} has registered. Anything else is either
 * client/protocol drift or active fuzzing. We tolerate a small grace (1-2
 * stray packets on session start: that's normal) and disconnect when the
 * rate crosses a threshold, recording it in the audit log.
 *
 * Config (emulator_settings):
 *   sec.unknown_packet.max         (default 5)   — strikes allowed per window
 *   sec.unknown_packet.window_sec  (default 30)  — sliding window length
 *   sec.unknown_packet.kick        (default 1)   — 0 to only audit, 1 to kick
 */
public final class UnknownPacketGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnknownPacketGuard.class);

    private static final ConcurrentHashMap<Integer, Deque<Long>> STATE = new ConcurrentHashMap<>();

    private UnknownPacketGuard() {
    }

    public static void observe(GameClient client, int headerId) {
        if (client == null) return;
        // Ogni invocazione e' un packet rejected dal dispatch (header non
        // registrato). Bump del contatore Prometheus a prescindere dal kick.
        try {
            HealthEndpoint.PACKETS_REJECTED.incrementAndGet();
        } catch (Throwable ignored) {
        }
        int userId = client.getHabbo() != null && client.getHabbo().getHabboInfo() != null
                ? client.getHabbo().getHabboInfo().getId() : -1;
        if (userId <= 0) return; // pre-auth garbage is normal during handshake

        // POLICY: niente kick automatico (vedi Wave "tolerant"). Il client
        // Nitro moderno manda alcuni header non registrati in Arcturus 3.5.5
        // che NON sono attacchi; sono solo packet di un protocollo piu' recente.
        // Default cambiato: kick=false, max=50, window=60s. Manteniamo solo
        // l'audit log per visibilita' senza mai chiudere connessioni utenti.
        int max = Math.max(1, Emulator.getConfig().getInt("sec.unknown_packet.max", 50));
        int windowSec = Math.max(5, Emulator.getConfig().getInt("sec.unknown_packet.window_sec", 60));
        boolean kick = Emulator.getConfig().getBoolean("sec.unknown_packet.kick", false);

        Deque<Long> q = STATE.computeIfAbsent(userId, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        long cutoff = now - windowSec * 1000L;

        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
            q.addLast(now);

            if (q.size() < max) return;

            // Threshold crossed — audit and (optionally) kick.
            String username = client.getHabbo().getHabboInfo().getUsername();
            String ip = "";
            try {
                if (client.getChannel() != null && client.getChannel().remoteAddress() instanceof java.net.InetSocketAddress) {
                    ip = ((java.net.InetSocketAddress) client.getChannel().remoteAddress()).getAddress().getHostAddress();
                }
            } catch (Exception ignored) {
            }
            AuditLog.record(userId, username, "PACKET_FUZZ_DETECTED", "user:" + userId,
                    "headers_in_window=" + q.size() + " window_sec=" + windowSec + " last_header=" + headerId + " ip=" + ip);
            LOGGER.warn("PACKET_FUZZ_RILEVATO utente={} headers={} last_id={} (kick={})", username, q.size(), headerId, kick);
            q.clear(); // reset, so we don't fire every packet after the threshold

            if (kick) {
                try {
                    client.dispose();
                } catch (Exception e) {
                    LOGGER.warn("Smaltimento del client fuzzing fallito {}", userId, e);
                }
            }
        }
    }

    /** Frees the per-user state on disconnect. */
    public static void onDisconnect(int userId) {
        if (userId > 0) STATE.remove(userId);
    }
}
