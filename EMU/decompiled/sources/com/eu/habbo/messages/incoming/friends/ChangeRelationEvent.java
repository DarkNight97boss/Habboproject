package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.UpdateFriendComposer;
import com.eu.habbo.plugin.events.users.friends.UserRelationShipEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/ChangeRelationEvent.class */
public class ChangeRelationEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        MessengerBuddy messengerBuddy = this.client.getHabbo().getMessenger().getFriends().get(Integer.valueOf(iIntValue));
        if (messengerBuddy == null || iIntValue2 < 0 || iIntValue2 > 3) {
            return;
        }
        UserRelationShipEvent userRelationShipEvent = new UserRelationShipEvent(this.client.getHabbo(), messengerBuddy, iIntValue2);
        if (userRelationShipEvent.isCancelled()) {
            return;
        }
        messengerBuddy.setRelation(userRelationShipEvent.relationShip);
        this.client.sendResponse(new UpdateFriendComposer(this.client.getHabbo(), messengerBuddy, (Integer) 0));
    }
}
