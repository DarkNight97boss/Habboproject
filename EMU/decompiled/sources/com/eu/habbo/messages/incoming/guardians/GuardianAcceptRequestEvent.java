package com.eu.habbo.messages.incoming.guardians;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guardians/GuardianAcceptRequestEvent.class */
public class GuardianAcceptRequestEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Emulator.getGameEnvironment().getGuideManager().acceptTicket(this.client.getHabbo(), this.packet.readBoolean());
    }
}
