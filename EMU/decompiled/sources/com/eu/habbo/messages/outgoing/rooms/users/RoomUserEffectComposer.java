package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserEffectComposer.class */
public class RoomUserEffectComposer extends MessageComposer {
    private final RoomUnit roomUnit;
    private final int effectId;

    public RoomUserEffectComposer(RoomUnit roomUnit) {
        this.roomUnit = roomUnit;
        this.effectId = -1;
    }

    public RoomUserEffectComposer(RoomUnit roomUnit, int i) {
        this.roomUnit = roomUnit;
        this.effectId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserEffectComposer);
        this.response.appendInt(Integer.valueOf(this.roomUnit.getId()));
        this.response.appendInt(Integer.valueOf(this.effectId == -1 ? this.roomUnit.getEffectId() : this.effectId));
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
