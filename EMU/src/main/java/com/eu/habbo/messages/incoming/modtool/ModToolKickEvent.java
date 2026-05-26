package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

public class ModToolKickEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        if (com.eu.habbo.core.StaffMfa.blockIfLocked(this.client)) return;
        Habbo target = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.packet.readInt());
        if (target != null && target.getHabboInfo().getRank().getId() >= this.client.getHabbo().getHabboInfo().getRank().getId()) {
            return; // refuse to kick equal/higher rank
        }
        Emulator.getGameEnvironment().getModToolManager().kick(this.client.getHabbo(), target, this.packet.readString());
    }
}
