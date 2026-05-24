package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserDataComposer.class */
public class RoomUserDataComposer extends MessageComposer {
    private final Habbo habbo;

    public RoomUserDataComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserDataComposer);
        this.response.appendInt(Integer.valueOf(this.habbo.getRoomUnit() == null ? -1 : this.habbo.getRoomUnit().getId()));
        this.response.appendString(this.habbo.getHabboInfo().getLook());
        this.response.appendString(this.habbo.getHabboInfo().getGender().name() + Emulator.PREVIEW);
        this.response.appendString(this.habbo.getHabboInfo().getMotto());
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().getAchievementScore()));
        return this.response;
    }
}
