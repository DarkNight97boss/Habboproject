package com.eu.habbo.plugin.events.users.friends;

import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.users.UserEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/friends/UserFriendEvent.class */
public abstract class UserFriendEvent extends UserEvent {
    public final MessengerBuddy friend;

    public UserFriendEvent(Habbo habbo, MessengerBuddy messengerBuddy) {
        super(habbo);
        this.friend = messengerBuddy;
    }
}
