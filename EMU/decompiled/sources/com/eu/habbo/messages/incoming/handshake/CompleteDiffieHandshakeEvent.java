package com.eu.habbo.messages.incoming.handshake;

import com.eu.habbo.Emulator;
import com.eu.habbo.crypto.HabboRC4;
import com.eu.habbo.messages.NoAuthMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.handshake.CompleteDiffieHandshakeComposer;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import com.eu.habbo.networking.gameserver.decoders.GameByteDecryption;
import com.eu.habbo.networking.gameserver.encoders.GameByteEncryption;
import io.netty.channel.ChannelHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/handshake/CompleteDiffieHandshakeEvent.class */
@NoAuthMessage
public class CompleteDiffieHandshakeEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getEncryption() == null) {
            Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
            return;
        }
        byte[] sharedKey = this.client.getEncryption().getDiffie().getSharedKey(this.packet.readString());
        this.client.setHandshakeFinished(true);
        this.client.sendResponse(new CompleteDiffieHandshakeComposer(this.client.getEncryption().getDiffie().getPublicKey()));
        this.client.getChannel().attr(GameServerAttributes.CRYPTO_CLIENT).set(new HabboRC4(sharedKey));
        this.client.getChannel().attr(GameServerAttributes.CRYPTO_SERVER).set(new HabboRC4(sharedKey));
        this.client.getChannel().pipeline().addFirst(new ChannelHandler[]{new GameByteDecryption()});
        this.client.getChannel().pipeline().addFirst(new ChannelHandler[]{new GameByteEncryption()});
    }
}
