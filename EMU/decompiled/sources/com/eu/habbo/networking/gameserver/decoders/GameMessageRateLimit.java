package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/decoders/GameMessageRateLimit.class */
public class GameMessageRateLimit extends MessageToMessageDecoder<ClientMessage> {
    private static final int RESET_TIME = 1;
    private static final int MAX_COUNTER = 10;

    protected void decode(ChannelHandlerContext channelHandlerContext, ClientMessage clientMessage, List<Object> list) throws Exception {
        GameClient gameClient = (GameClient) channelHandlerContext.channel().attr(GameServerAttributes.CLIENT).get();
        if (gameClient == null) {
            return;
        }
        int iIntValue = 0;
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        if (((long) intUnixTimestamp) - gameClient.lastPacketCounterCleared > 1) {
            gameClient.incomingPacketCounter.clear();
            gameClient.lastPacketCounterCleared = intUnixTimestamp;
        } else {
            iIntValue = gameClient.incomingPacketCounter.getOrDefault(Integer.valueOf(clientMessage.getMessageId()), 0).intValue();
        }
        if (iIntValue > 10) {
            return;
        }
        gameClient.incomingPacketCounter.put(Integer.valueOf(clientMessage.getMessageId()), Integer.valueOf(iIntValue + 1));
        list.add(clientMessage);
    }

    protected /* bridge */ /* synthetic */ void decode(ChannelHandlerContext channelHandlerContext, Object obj, List list) throws Exception {
        decode(channelHandlerContext, (ClientMessage) obj, (List<Object>) list);
    }
}
