package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.friends.UserFriendChatEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/FriendPrivateMessageEvent.class */
public class FriendPrivateMessageEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        if (this.client.getHabbo().getHabboStats().allowTalk()) {
            long jCurrentTimeMillis = System.currentTimeMillis();
            if (jCurrentTimeMillis - this.client.getHabbo().getHabboStats().lastChat < 750) {
                return;
            }
            this.client.getHabbo().getHabboStats().lastChat = jCurrentTimeMillis;
            MessengerBuddy friend = this.client.getHabbo().getMessenger().getFriend(iIntValue);
            if (friend == null) {
                return;
            }
            if (string.length() > 255) {
                string = string.substring(0, 255);
            }
            if (((UserFriendChatEvent) Emulator.getPluginManager().fireEvent(new UserFriendChatEvent(this.client.getHabbo(), friend, string))).isCancelled()) {
                return;
            }
            friend.onMessageReceived(this.client.getHabbo(), string);
        }
    }
}
