package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolCloseTicketEvent.class */
public class ModToolCloseTicketEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.ticket.close").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
        }
        int iIntValue = this.packet.readInt().intValue();
        this.packet.readInt().intValue();
        ModToolIssue ticket = Emulator.getGameEnvironment().getModToolManager().getTicket(this.packet.readInt().intValue());
        if (ticket == null || ticket.modId != this.client.getHabbo().getHabboInfo().getId()) {
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(ticket.senderId);
        switch (iIntValue) {
            case 1:
                Emulator.getGameEnvironment().getModToolManager().closeTicketAsUseless(ticket, habbo);
                break;
            case 2:
                Emulator.getGameEnvironment().getModToolManager().closeTicketAsAbusive(ticket, habbo);
                break;
            case 3:
                Emulator.getGameEnvironment().getModToolManager().closeTicketAsHandled(ticket, habbo);
                break;
        }
    }
}
