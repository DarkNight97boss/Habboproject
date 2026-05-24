package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class GameMessageRateLimit extends MessageToMessageDecoder<ClientMessage> {

    private final int resetTime;
    private final int maxPerId;
    private final int maxTotal;
    private final int maxViolations;
    private final int maxPreAuthPackets;

    public GameMessageRateLimit() {
        this.resetTime = Emulator.getConfig().getInt("networking.ratelimit.reset.seconds", 1);
        this.maxPerId = Emulator.getConfig().getInt("networking.ratelimit.max.per.id", 10);
        this.maxTotal = Emulator.getConfig().getInt("networking.ratelimit.max.total", 200);
        this.maxViolations = Emulator.getConfig().getInt("networking.ratelimit.max.violations", 10);
        this.maxPreAuthPackets = Emulator.getConfig().getInt("networking.auth.max.packets", 50);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ClientMessage message, List<Object> out) throws Exception {
        GameClient client = ctx.channel().attr(GameServerAttributes.CLIENT).get();

        if (client == null) {
            return;
        }

        // Cap the number of packets accepted before the client authenticates (anti SSO/handshake flood).
        if (this.maxPreAuthPackets > 0 && client.getHabbo() == null) {
            if (++client.preAuthPacketCount > this.maxPreAuthPackets) {
                ctx.close();
                return;
            }
        }

        // Reset the per-second window.
        int timestamp = Emulator.getIntUnixTimestamp();
        if (timestamp - client.lastPacketCounterCleared > this.resetTime) {
            // Track consecutive windows that breached the global cap, so sustained floods get disconnected.
            if (this.maxTotal > 0 && client.incomingPacketTotal > this.maxTotal) {
                client.rateLimitViolations++;
            } else {
                client.rateLimitViolations = 0;
            }
            client.incomingPacketCounter.clear();
            client.incomingPacketTotal = 0;
            client.lastPacketCounterCleared = timestamp;
        }

        // Global per-connection cap (covers floods of varied packet ids that the per-id cap misses).
        if (this.maxTotal > 0) {
            client.incomingPacketTotal++;
            if (client.incomingPacketTotal > this.maxTotal) {
                if (this.maxViolations > 0 && client.rateLimitViolations >= this.maxViolations) {
                    ctx.close();
                }
                return;
            }
        }

        // Per-id cap (original behaviour).
        int count = client.incomingPacketCounter.getOrDefault(message.getMessageId(), 0);
        if (count > this.maxPerId) {
            return;
        }
        client.incomingPacketCounter.put(message.getMessageId(), count + 1);

        // Continue processing.
        out.add(message);
    }

}
