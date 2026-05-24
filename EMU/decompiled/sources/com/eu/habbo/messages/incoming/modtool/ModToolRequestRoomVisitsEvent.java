package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolUserRoomVisitsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolRequestRoomVisitsEvent.class */
public class ModToolRequestRoomVisitsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue;
        HabboInfo habboInfo;
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL) || (habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo((iIntValue = this.packet.readInt().intValue()))) == null) {
            return;
        }
        this.client.sendResponse(new ModToolUserRoomVisitsComposer(habboInfo, Emulator.getGameEnvironment().getModToolManager().getUserRoomVisits(iIntValue)));
    }
}
