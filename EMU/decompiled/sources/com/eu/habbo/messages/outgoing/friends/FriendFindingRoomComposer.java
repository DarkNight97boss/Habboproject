package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/FriendFindingRoomComposer.class */
public class FriendFindingRoomComposer extends MessageComposer {
    public static final int NO_ROOM_FOUND = 0;
    public static final int ROOM_FOUND = 1;
    private final int errorCode;

    public FriendFindingRoomComposer(int i) {
        this.errorCode = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(1210);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        return this.response;
    }
}
