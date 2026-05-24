package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.RemoveFriendComposer;
import gnu.trove.list.array.TIntArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/RemoveFriendEvent.class */
public class RemoveFriendEvent extends MessageHandler {
    private final TIntArrayList removedFriends = new TIntArrayList();

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        for (int i = 0; i < iIntValue; i++) {
            int iIntValue2 = this.packet.readInt().intValue();
            this.removedFriends.add(iIntValue2);
            Messenger.unfriend(this.client.getHabbo().getHabboInfo().getId(), iIntValue2);
            this.client.getHabbo().getMessenger().removeBuddy(iIntValue2);
            Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(iIntValue2);
            if (habbo != null) {
                habbo.getMessenger().removeBuddy(this.client.getHabbo());
                habbo.getClient().sendResponse(new RemoveFriendComposer(this.client.getHabbo().getHabboInfo().getId()));
            }
        }
        this.client.sendResponse(new RemoveFriendComposer(this.removedFriends));
    }
}
