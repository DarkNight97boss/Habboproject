package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolKickEvent.class */
public class ModToolKickEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Emulator.getGameEnvironment().getModToolManager().kick(this.client.getHabbo(), Emulator.getGameEnvironment().getHabboManager().getHabbo(this.packet.readInt().intValue()), this.packet.readString());
    }
}
