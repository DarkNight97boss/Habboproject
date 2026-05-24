package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserReceivedHandItemComposer.class */
public class RoomUserReceivedHandItemComposer extends MessageComposer {
    private RoomUnit from;
    private int handItem;

    public RoomUserReceivedHandItemComposer(RoomUnit roomUnit, int i) {
        this.from = roomUnit;
        this.handItem = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserReceivedHandItemComposer);
        this.response.appendInt(Integer.valueOf(this.from.getId()));
        this.response.appendInt(Integer.valueOf(this.handItem));
        return this.response;
    }
}
