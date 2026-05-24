package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.habbohotel.users.HabboNavigatorWindowSettings;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/NewNavigatorSettingsComposer.class */
public class NewNavigatorSettingsComposer extends MessageComposer {
    private final HabboNavigatorWindowSettings windowSettings;

    public NewNavigatorSettingsComposer(HabboNavigatorWindowSettings habboNavigatorWindowSettings) {
        this.windowSettings = habboNavigatorWindowSettings;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewNavigatorSettingsComposer);
        this.response.appendInt(Integer.valueOf(this.windowSettings.x));
        this.response.appendInt(Integer.valueOf(this.windowSettings.y));
        this.response.appendInt(Integer.valueOf(this.windowSettings.width));
        this.response.appendInt(Integer.valueOf(this.windowSettings.height));
        this.response.appendBoolean(Boolean.valueOf(this.windowSettings.openSearches));
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
