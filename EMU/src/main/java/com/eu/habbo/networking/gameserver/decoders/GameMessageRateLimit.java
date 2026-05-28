package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class GameMessageRateLimit extends MessageToMessageDecoder<ClientMessage> {

    private static final int RESET_TIME = 1;
    private static final int MAX_COUNTER = 30;        // per-id: 30 packets/sec dello stesso id -> DROPPA (no kick)
    private static final int MAX_TOTAL = 1000;        // aggregate: 1000 packets/sec totali -> DROPPA (no kick)
    private static final int AGGREGATE_KEY = Integer.MIN_VALUE; // sentinel key (won't collide with real message ids)

    @Override
    protected void decode(ChannelHandlerContext ctx, ClientMessage message, List<Object> out) throws Exception {
        GameClient client = ctx.channel().attr(GameServerAttributes.CLIENT).get();

        if (client == null) {
            return;
        }

        // Pre-auth packet cap: cumulative across the whole pre-login window.
        // Caps how many packets a connection can send BEFORE authentication, to
        // limit SSO spam / DB amplification on the login path. Compatible with
        // the "tolerant" policy: we DROP, never close (the auth timeout in
        // GameClientManager handles the time dimension). Config:
        // networking.auth.max.packets (default 50, 0 = disabled).
        if (client.getHabbo() == null) {
            int maxPreAuth = Emulator.getConfig().getInt("networking.auth.max.packets", 50);
            if (maxPreAuth > 0 && ++client.preAuthPacketCount > maxPreAuth) {
                return; // drop, no close
            }
        }

        // Check if reset time has passed.
        int timestamp = Emulator.getIntUnixTimestamp();
        if (timestamp - client.lastPacketCounterCleared > RESET_TIME) {
            // Reset counter.
            client.incomingPacketCounter.clear();
            client.lastPacketCounterCleared = timestamp;
        }

        // POLICY (Wave "tolerant"): niente kick. Mai. Se l'aggregate supera la
        // soglia, DROPPA il packet e basta. Un attaccante che martella il
        // server al massimo consuma il suo turno di CPU per la decode finche'
        // non si stanca; il network thread non e' bloccato. Soglie alzate
        // (30 per-id, 1000 totali al secondo) per evitare anche solo drop su
        // un client legit che ha appena cambiato stanza affollata.
        int total = client.incomingPacketCounter.getOrDefault(AGGREGATE_KEY, 0) + 1;
        client.incomingPacketCounter.put(AGGREGATE_KEY, total);
        if (total > MAX_TOTAL) {
            // DROP, niente ctx.close().
            return;
        }

        // Per message-id limit -> droppa il packet, ma niente close.
        int count = client.incomingPacketCounter.getOrDefault(message.getMessageId(), 0);
        if (count > MAX_COUNTER) {
            return;
        }

        client.incomingPacketCounter.put(message.getMessageId(), ++count);

        // Continue processing.
        out.add(message);
    }

}
