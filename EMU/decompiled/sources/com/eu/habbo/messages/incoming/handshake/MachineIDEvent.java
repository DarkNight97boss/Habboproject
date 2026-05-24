package com.eu.habbo.messages.incoming.handshake;

import com.eu.habbo.messages.NoAuthMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.handshake.MachineIDComposer;
import com.eu.habbo.util.HexUtils;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/handshake/MachineIDEvent.class */
@NoAuthMessage
public class MachineIDEvent extends MessageHandler {
    private static final int HASH_LENGTH = 64;

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        this.packet.readString();
        this.packet.readString();
        if (string.startsWith("~") || string.length() != HASH_LENGTH) {
            string = HexUtils.getRandom(HASH_LENGTH);
            this.client.sendResponse(new MachineIDComposer(string));
        }
        this.client.setMachineId(string);
    }
}
