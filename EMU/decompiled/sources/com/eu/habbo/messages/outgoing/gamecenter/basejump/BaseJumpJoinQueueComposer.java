package com.eu.habbo.messages.outgoing.gamecenter.basejump;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/gamecenter/basejump/BaseJumpJoinQueueComposer.class */
public class BaseJumpJoinQueueComposer extends MessageComposer {
    private final int gameId;

    public BaseJumpJoinQueueComposer(int i) {
        this.gameId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BaseJumpJoinQueueComposer);
        this.response.appendInt(Integer.valueOf(this.gameId));
        return this.response;
    }
}
