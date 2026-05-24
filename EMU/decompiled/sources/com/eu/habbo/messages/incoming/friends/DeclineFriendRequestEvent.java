package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/DeclineFriendRequestEvent.class */
public class DeclineFriendRequestEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.packet.readBoolean()) {
            this.client.getHabbo().getMessenger().deleteAllFriendRequests(this.client.getHabbo().getHabboInfo().getId());
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        for (int i = 0; i < iIntValue; i++) {
            this.client.getHabbo().getMessenger().deleteFriendRequests(this.packet.readInt().intValue(), this.client.getHabbo().getHabboInfo().getId());
        }
    }
}
