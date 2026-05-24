package com.eu.habbo.messages.incoming.ambassadors;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.plugin.events.support.SupportUserAlertedEvent;
import com.eu.habbo.plugin.events.support.SupportUserAlertedReason;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/ambassadors/AmbassadorAlertCommandEvent.class */
public class AmbassadorAlertCommandEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_AMBASSADOR)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.alert").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%message%", "${notification.ambassador.alert.warning.message}"));
            return;
        }
        Habbo habbo = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(this.packet.readInt().intValue());
        if (habbo == null) {
            return;
        }
        if (((SupportUserAlertedEvent) Emulator.getPluginManager().fireEvent(new SupportUserAlertedEvent(this.client.getHabbo(), habbo, "${notification.ambassador.alert.warning.message}", SupportUserAlertedReason.AMBASSADOR))).isCancelled()) {
            return;
        }
        habbo.getClient().sendResponse(new BubbleAlertComposer("ambassador.alert.warning"));
    }
}
