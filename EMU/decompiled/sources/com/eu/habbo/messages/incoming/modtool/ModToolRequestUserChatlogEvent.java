package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolUserChatlogComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolRequestUserChatlogEvent.class */
public class ModToolRequestUserChatlogEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.chatlog").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        this.client.sendResponse(new ModToolUserChatlogComposer(Emulator.getGameEnvironment().getModToolManager().getUserRoomVisitsAndChatlogs(iIntValue), iIntValue, HabboManager.getOfflineHabboInfo(iIntValue).getUsername()));
    }
}
