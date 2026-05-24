package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.CfhTopic;
import com.eu.habbo.habbohotel.modtool.ModToolBanType;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolPreset;
import com.eu.habbo.habbohotel.modtool.ModToolTicketState;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolIssueDefaultSanctionEvent.class */
public class ModToolIssueDefaultSanctionEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        ModToolPreset modToolPreset;
        if (this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            int iIntValue = this.packet.readInt().intValue();
            this.packet.readInt().intValue();
            int iIntValue2 = this.packet.readInt().intValue();
            ModToolIssue ticket = Emulator.getGameEnvironment().getModToolManager().getTicket(iIntValue);
            if (ticket.modId != this.client.getHabbo().getHabboInfo().getId()) {
                this.client.getHabbo().alert(Emulator.getTexts().getValue("supporttools.not_ticket_owner"));
                return;
            }
            CfhTopic cfhTopic = Emulator.getGameEnvironment().getModToolManager().getCfhTopic(iIntValue2);
            if (cfhTopic != null && (modToolPreset = cfhTopic.defaultSanction) != null) {
                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(ticket.reportedId);
                if (modToolPreset.banLength > 0) {
                    Emulator.getGameEnvironment().getModToolManager().ban(ticket.reportedId, this.client.getHabbo(), modToolPreset.message, modToolPreset.banLength * 86400, ModToolBanType.ACCOUNT, cfhTopic.id);
                } else if (modToolPreset.muteLength > 0 && habbo != null) {
                    habbo.mute(modToolPreset.muteLength * 86400, false);
                }
            }
            ticket.state = ModToolTicketState.CLOSED;
            Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(ticket);
        }
    }
}
