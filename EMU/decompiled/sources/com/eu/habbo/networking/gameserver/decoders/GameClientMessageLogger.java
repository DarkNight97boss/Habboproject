package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.PacketNames;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/decoders/GameClientMessageLogger.class */
public class GameClientMessageLogger extends MessageToMessageDecoder<ClientMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameClientMessageLogger.class);
    private final PacketNames names = Emulator.getGameServer().getPacketManager().getNames();

    protected void decode(ChannelHandlerContext channelHandlerContext, ClientMessage clientMessage, List<Object> list) {
        LOGGER.debug(String.format("[\u001b[32mCLIENT\u001b[39m][%-4d][%-41s] => %s", Integer.valueOf(clientMessage.getMessageId()), this.names.getIncomingName(clientMessage.getMessageId()), clientMessage.getMessageBody()));
        list.add(clientMessage);
    }

    protected /* bridge */ /* synthetic */ void decode(ChannelHandlerContext channelHandlerContext, Object obj, List list) throws Exception {
        decode(channelHandlerContext, (ClientMessage) obj, (List<Object>) list);
    }
}
