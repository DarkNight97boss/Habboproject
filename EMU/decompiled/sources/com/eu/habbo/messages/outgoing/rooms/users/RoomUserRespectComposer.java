package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserRespectComposer.class */
public class RoomUserRespectComposer extends MessageComposer {
    private final Habbo habbo;

    public RoomUserRespectComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserRespectComposer);
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboInfo().getId()));
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().respectPointsReceived));
        return this.response;
    }
}
