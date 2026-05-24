package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/StalkErrorComposer.class */
public class StalkErrorComposer extends MessageComposer {
    public static final int NOT_IN_FRIEND_LIST = 0;
    public static final int FRIEND_OFFLINE = 1;
    public static final int FRIEND_NOT_IN_ROOM = 2;
    public static final int FRIEND_BLOCKED_STALKING = 3;
    private final int errorCode;

    public StalkErrorComposer(int i) {
        this.errorCode = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.StalkErrorComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        return this.response;
    }
}
