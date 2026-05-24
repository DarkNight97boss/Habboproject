package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserHomeRoomComposer.class */
public class UserHomeRoomComposer extends MessageComposer {
    private final int homeRoom;
    private final int roomToEnter;

    public UserHomeRoomComposer(int i, int i2) {
        this.homeRoom = i;
        this.roomToEnter = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserHomeRoomComposer);
        this.response.appendInt(Integer.valueOf(this.homeRoom));
        this.response.appendInt(Integer.valueOf(this.roomToEnter));
        return this.response;
    }
}
