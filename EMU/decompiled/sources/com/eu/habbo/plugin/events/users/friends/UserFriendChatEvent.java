package com.eu.habbo.plugin.events.users.friends;

import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/friends/UserFriendChatEvent.class */
public class UserFriendChatEvent extends UserFriendEvent {
    public String message;

    public UserFriendChatEvent(Habbo habbo, MessengerBuddy messengerBuddy, String str) {
        super(habbo, messengerBuddy);
        this.message = str;
    }
}
