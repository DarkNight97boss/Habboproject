package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/MeMenuSettingsComposer.class */
public class MeMenuSettingsComposer extends MessageComposer {
    private final Habbo habbo;

    public MeMenuSettingsComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MeMenuSettingsComposer);
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().volumeSystem));
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().volumeFurni));
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().volumeTrax));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.getHabboStats().preferOldChat));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.getHabboStats().blockRoomInvites));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.getHabboStats().blockCameraFollow));
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().uiFlags));
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().chatColor.getType()));
        return this.response;
    }
}
