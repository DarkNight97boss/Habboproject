package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.StalkErrorComposer;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/StalkFriendEvent.class */
public class StalkFriendEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getMessenger().getFriend(iIntValue) == null) {
            this.client.sendResponse(new StalkErrorComposer(0));
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue);
        if (habbo == null || !habbo.isOnline()) {
            this.client.sendResponse(new StalkErrorComposer(1));
            return;
        }
        if (habbo.getHabboStats().blockFollowing && !this.client.getHabbo().hasPermission("acc_can_stalk")) {
            this.client.sendResponse(new StalkErrorComposer(3));
            return;
        }
        if (habbo.getHabboInfo().getCurrentRoom() == null) {
            this.client.sendResponse(new StalkErrorComposer(2));
        } else if (habbo.getHabboInfo().getCurrentRoom() != this.client.getHabbo().getHabboInfo().getCurrentRoom()) {
            this.client.sendResponse(new ForwardToRoomComposer(habbo.getHabboInfo().getCurrentRoom().getId()));
        } else {
            this.client.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(Emulator.getTexts().getValue("stalk.failed.same.room").replace("%user%", habbo.getHabboInfo().getUsername()), this.client.getHabbo(), this.client.getHabbo(), RoomChatMessageBubbles.ALERT)));
        }
    }
}
