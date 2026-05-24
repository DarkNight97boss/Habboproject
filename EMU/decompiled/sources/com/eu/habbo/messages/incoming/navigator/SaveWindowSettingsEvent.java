package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.habbohotel.users.HabboNavigatorWindowSettings;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/SaveWindowSettingsEvent.class */
public class SaveWindowSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboNavigatorWindowSettings habboNavigatorWindowSettings = this.client.getHabbo().getHabboStats().navigatorWindowSettings;
        habboNavigatorWindowSettings.x = this.packet.readInt().intValue();
        habboNavigatorWindowSettings.y = this.packet.readInt().intValue();
        habboNavigatorWindowSettings.width = this.packet.readInt().intValue();
        habboNavigatorWindowSettings.height = this.packet.readInt().intValue();
        habboNavigatorWindowSettings.openSearches = this.packet.readBoolean();
        this.packet.readInt().intValue();
    }
}
