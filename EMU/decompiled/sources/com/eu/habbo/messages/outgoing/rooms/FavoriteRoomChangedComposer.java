package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/FavoriteRoomChangedComposer.class */
public class FavoriteRoomChangedComposer extends MessageComposer {
    private final int roomId;
    private final boolean favorite;

    public FavoriteRoomChangedComposer(int i, boolean z) {
        this.roomId = i;
        this.favorite = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FavoriteRoomChangedComposer);
        this.response.appendInt(Integer.valueOf(this.roomId));
        this.response.appendBoolean(Boolean.valueOf(this.favorite));
        return this.response;
    }
}
