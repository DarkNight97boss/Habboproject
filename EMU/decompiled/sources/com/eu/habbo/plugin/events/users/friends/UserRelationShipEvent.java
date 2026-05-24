package com.eu.habbo.plugin.events.users.friends;

import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/friends/UserRelationShipEvent.class */
public class UserRelationShipEvent extends UserFriendEvent {
    public int relationShip;

    public UserRelationShipEvent(Habbo habbo, MessengerBuddy messengerBuddy, int i) {
        super(habbo, messengerBuddy);
        this.relationShip = i;
    }
}
