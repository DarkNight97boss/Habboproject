package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketState;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueInfoComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolPickTicketEvent.class */
public class ModToolPickTicketEvent extends MessageHandler {
    public static boolean send = false;

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.ticket.pick").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
            return;
        }
        this.packet.readInt();
        ModToolIssue ticket = Emulator.getGameEnvironment().getModToolManager().getTicket(this.packet.readInt().intValue());
        if (ticket == null) {
            this.client.getHabbo().alert(Emulator.getTexts().getValue("support.ticket.picked.failed"));
        } else if (ticket.state != ModToolTicketState.PICKED) {
            Emulator.getGameEnvironment().getModToolManager().pickTicket(ticket, this.client.getHabbo());
        } else {
            this.client.sendResponse(new ModToolIssueInfoComposer(ticket));
            this.client.getHabbo().alert(Emulator.getTexts().getValue("support.ticket.picked.failed"));
        }
    }
}
