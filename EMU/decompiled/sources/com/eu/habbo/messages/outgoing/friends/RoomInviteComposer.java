package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/RoomInviteComposer.class */
public class RoomInviteComposer extends MessageComposer {
    private final int userId;
    private final String message;

    public RoomInviteComposer(int i, String str) {
        this.userId = i;
        this.message = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomInviteComposer);
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendString(this.message);
        return this.response;
    }
}
