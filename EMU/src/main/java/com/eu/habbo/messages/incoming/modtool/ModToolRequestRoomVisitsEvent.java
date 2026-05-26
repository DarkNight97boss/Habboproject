package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolUserRoomVisitsComposer;

public class ModToolRequestRoomVisitsEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        if (com.eu.habbo.core.StaffMfa.blockIfLocked(this.client)) return;
        if (this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            int userId = this.packet.readInt();

            HabboInfo habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(userId);

            if (habboInfo != null) {
                this.client.sendResponse(new ModToolUserRoomVisitsComposer(habboInfo, Emulator.getGameEnvironment().getModToolManager().getUserRoomVisits(userId)));
            }
        } else {
            // Symmetric with the other ModTool* handlers: non-privileged packet
            // submissions are scripter signals and should be alerted.
            ScripterManager.scripterDetected(this.client,
                    Emulator.getTexts().getValue("scripter.warning.modtools.kick")
                            .replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
        }
    }
}
