package com.eu.habbo.networking.gameserver.encoders;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.PacketNames;
import com.eu.habbo.messages.ServerMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/encoders/GameServerMessageLogger.class */
public class GameServerMessageLogger extends MessageToMessageEncoder<ServerMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameServerMessageLogger.class);
    private final PacketNames names = Emulator.getGameServer().getPacketManager().getNames();

    protected void encode(ChannelHandlerContext channelHandlerContext, ServerMessage serverMessage, List<Object> list) {
        LOGGER.debug(String.format("[\u001b[34mSERVER\u001b[39m][%-4d][%-41s] => %s", Integer.valueOf(serverMessage.getHeader()), this.names.getOutgoingName(serverMessage.getHeader()), serverMessage.getBodyString()));
        list.add(serverMessage);
    }

    protected /* bridge */ /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object obj, List list) throws Exception {
        encode(channelHandlerContext, (ServerMessage) obj, (List<Object>) list);
    }
}
