package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/FriendRequestErrorComposer.class */
public class FriendRequestErrorComposer extends MessageComposer {
    public static final int FRIEND_LIST_OWN_FULL = 1;
    public static final int FRIEND_LIST_TARGET_FULL = 2;
    public static final int TARGET_NOT_ACCEPTING_REQUESTS = 3;
    public static final int TARGET_NOT_FOUND = 4;
    private final int errorCode;

    public FriendRequestErrorComposer(int i) {
        this.errorCode = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FriendRequestErrorComposer);
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        return this.response;
    }
}
