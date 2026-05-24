package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomScoreComposer.class */
public class RoomScoreComposer extends MessageComposer {
    private final int score;
    private final boolean canVote;

    public RoomScoreComposer(int i, boolean z) {
        this.score = i;
        this.canVote = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomScoreComposer);
        this.response.appendInt(Integer.valueOf(this.score));
        this.response.appendBoolean(Boolean.valueOf(this.canVote));
        return this.response;
    }
}
