package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketState;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolReleaseTicketEvent.class */
public class ModToolReleaseTicketEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.ticket.release").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        while (iIntValue != 0) {
            iIntValue--;
            ModToolIssue ticket = Emulator.getGameEnvironment().getModToolManager().getTicket(this.packet.readInt().intValue());
            if (ticket != null && ticket.modId == this.client.getHabbo().getHabboInfo().getId()) {
                ticket.modId = 0;
                ticket.modName = Emulator.PREVIEW;
                ticket.state = ModToolTicketState.OPEN;
                Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(ticket);
            }
        }
    }
}
