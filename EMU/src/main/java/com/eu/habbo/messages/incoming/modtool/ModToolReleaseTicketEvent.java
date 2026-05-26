package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketState;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.incoming.MessageHandler;

public class ModToolReleaseTicketEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        if (this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            // Bound from the client. `while (count != 0) count--` with count = -1 or
            // Integer.MIN_VALUE iterates ~2 billion times before underflowing back to 0,
            // burning the netty IO thread.
            int count = Math.max(0, Math.min(this.packet.readInt(), 200));

            for (int i = 0; i < count; i++) {
                int ticketId = this.packet.readInt();

                ModToolIssue issue = Emulator.getGameEnvironment().getModToolManager().getTicket(ticketId);

                if (issue == null)
                    continue;

                if (issue.modId != this.client.getHabbo().getHabboInfo().getId())
                    continue;

                issue.modId = 0;
                issue.modName = "";
                issue.state = ModToolTicketState.OPEN;

                Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(issue);
            }
        } else {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.ticket.release").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
        }
    }
}
