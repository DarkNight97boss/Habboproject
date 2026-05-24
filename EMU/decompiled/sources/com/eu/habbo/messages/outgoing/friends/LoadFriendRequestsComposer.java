package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.habbohotel.messenger.FriendRequest;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/LoadFriendRequestsComposer.class */
public class LoadFriendRequestsComposer extends MessageComposer {
    private final Habbo habbo;

    public LoadFriendRequestsComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.LoadFriendRequestsComposer);
        synchronized (this.habbo.getMessenger().getFriendRequests()) {
            this.response.appendInt(Integer.valueOf(this.habbo.getMessenger().getFriendRequests().size()));
            this.response.appendInt(Integer.valueOf(this.habbo.getMessenger().getFriendRequests().size()));
            TObjectHashIterator it = this.habbo.getMessenger().getFriendRequests().iterator();
            while (it.hasNext()) {
                FriendRequest friendRequest = (FriendRequest) it.next();
                this.response.appendInt(Integer.valueOf(friendRequest.getId()));
                this.response.appendString(friendRequest.getUsername());
                this.response.appendString(friendRequest.getLook());
            }
        }
        return this.response;
    }
}
