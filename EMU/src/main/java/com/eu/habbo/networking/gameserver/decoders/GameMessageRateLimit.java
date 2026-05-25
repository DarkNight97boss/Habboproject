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
    private static final int MAX_COUNTER = 10;        // max packets of a single message-id per RESET_TIME
    private static final int MAX_TOTAL = 500;         // max packets across ALL message-ids per RESET_TIME -> disconnect
    private static final int AGGREGATE_KEY = Integer.MIN_VALUE; // sentinel key (won't collide with real message ids)

    @Override
    protected void decode(ChannelHandlerContext ctx, ClientMessage message, List<Object> out) throws Exception {
        GameClient client = ctx.channel().attr(GameServerAttributes.CLIENT).get();

        if (client == null) {
            return;
        }

        // Check if reset time has passed.
        int timestamp = Emulator.getIntUnixTimestamp();
        if (timestamp - client.lastPacketCounterCleared > RESET_TIME) {
            // Reset counter.
            client.incomingPacketCounter.clear();
            client.lastPacketCounterCleared = timestamp;
        }

        // Aggregate flood check across ALL message-ids -> disconnect on gross excess
        // (catches header-rotation floods that the per-id limit alone would miss).
        int total = client.incomingPacketCounter.getOrDefault(AGGREGATE_KEY, 0) + 1;
        client.incomingPacketCounter.put(AGGREGATE_KEY, total);
        if (total > MAX_TOTAL) {
            ctx.close();
            return;
        }

        // Per message-id limit -> drop the offending packet.
        int count = client.incomingPacketCounter.getOrDefault(message.getMessageId(), 0);
        if (count > MAX_COUNTER) {
            return;
        }

        client.incomingPacketCounter.put(message.getMessageId(), ++count);

        // Continue processing.
        out.add(message);
    }

}
