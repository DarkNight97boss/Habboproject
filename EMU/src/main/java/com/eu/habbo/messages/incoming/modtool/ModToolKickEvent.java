package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;

public class ModToolKickEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        if (com.eu.habbo.core.StaffMfa.blockIfLocked(this.client)) return;
        Emulator.getGameEnvironment().getModToolManager().kick(this.client.getHabbo(), Emulator.getGameEnvironment().getHabboManager().getHabbo(this.packet.readInt()), this.packet.readString());
    }
}
