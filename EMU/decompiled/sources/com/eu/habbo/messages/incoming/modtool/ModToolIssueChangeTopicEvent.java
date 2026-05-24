package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.threading.runnables.UpdateModToolIssue;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolIssueChangeTopicEvent.class */
public class ModToolIssueChangeTopicEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            int iIntValue = this.packet.readInt().intValue();
            this.packet.readInt().intValue();
            int iIntValue2 = this.packet.readInt().intValue();
            ModToolIssue ticket = Emulator.getGameEnvironment().getModToolManager().getTicket(iIntValue);
            if (ticket != null) {
                ticket.category = iIntValue2;
                new UpdateModToolIssue(ticket).run();
                Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(ticket);
            }
        }
    }
}
