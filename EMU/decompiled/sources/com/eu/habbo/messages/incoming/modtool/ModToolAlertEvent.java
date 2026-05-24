package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.support.SupportUserAlertedReason;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolAlertEvent.class */
public class ModToolAlertEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.kick").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.packet.readInt().intValue());
        if (habbo != null) {
            Emulator.getGameEnvironment().getModToolManager().alert(this.client.getHabbo(), habbo, this.packet.readString(), SupportUserAlertedReason.ALERT);
        }
    }
}
