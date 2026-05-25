package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.messages.incoming.MessageHandler;

public class DeclineFriendRequestEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        boolean all = this.packet.readBoolean();

        if (all) {
            this.client.getHabbo().getMessenger().deleteAllFriendRequests(this.client.getHabbo().getHabboInfo().getId());
        } else {
            int count = Math.min(this.packet.readInt(), 200); // bound client count (anti-DoS, per-iter DB deletes)

            for (int i = 0; i < count; i++) {
                this.client.getHabbo().getMessenger().deleteFriendRequests(this.packet.readInt(), this.client.getHabbo().getHabboInfo().getId());
            }
        }
    }
}