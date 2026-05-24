package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.list.array.TIntArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/RemoveFriendComposer.class */
public class RemoveFriendComposer extends MessageComposer {
    private final TIntArrayList unfriendIds;

    public RemoveFriendComposer(TIntArrayList tIntArrayList) {
        this.unfriendIds = tIntArrayList;
    }

    public RemoveFriendComposer(int i) {
        this.unfriendIds = new TIntArrayList();
        this.unfriendIds.add(i);
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UpdateFriendComposer);
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(this.unfriendIds.size()));
        for (int i = 0; i < this.unfriendIds.size(); i++) {
            this.response.appendInt((Integer) (-1));
            this.response.appendInt(Integer.valueOf(this.unfriendIds.get(i)));
        }
        return this.response;
    }
}
