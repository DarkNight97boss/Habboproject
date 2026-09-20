package com.eu.habbo.messages.incoming.handshake;

import com.eu.habbo.Emulator;
import com.eu.habbo.crypto.HabboRC4;
import com.eu.habbo.messages.NoAuthMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.handshake.CompleteDiffieHandshakeComposer;
import com.eu.habbo.networking.gameserver.decoders.GameByteDecryption;
import com.eu.habbo.networking.gameserver.encoders.GameByteEncryption;
import com.eu.habbo.networking.gameserver.GameServerAttributes;

@NoAuthMessage
public class CompleteDiffieHandshakeEvent extends MessageHandler {

    @Override
    public void handle() throws Exception {
        if (this.client.getEncryption() == null) {
            Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
            return;
        }

        // Pentest DoS 2026-09-20: il campo DH e' un singolo BigInteger; un input enorme
        // costringerebbe RSA DoDecrypt a ciclare su molti blocchi (modPow con chiave
        // privata) PRIMA dell'autenticazione. Cap generoso: un pubkey DH legittimo sta
        // ampiamente sotto, l'attacco da ~200KB (centinaia di blocchi) e' neutralizzato.
        String diffiePublicKey = this.packet.readString();
        if (diffiePublicKey == null || diffiePublicKey.length() > 8192) {
            return;
        }
        byte[] sharedKey = this.client.getEncryption().getDiffie().getSharedKey(diffiePublicKey);

        this.client.setHandshakeFinished(true);
        this.client.sendResponse(new CompleteDiffieHandshakeComposer(this.client.getEncryption().getDiffie().getPublicKey()));

        this.client.getChannel().attr(GameServerAttributes.CRYPTO_CLIENT).set(new HabboRC4(sharedKey));
        this.client.getChannel().attr(GameServerAttributes.CRYPTO_SERVER).set(new HabboRC4(sharedKey));

        this.client.getChannel().pipeline().addFirst(new GameByteDecryption());
        this.client.getChannel().pipeline().addFirst(new GameByteEncryption());
    }

}
