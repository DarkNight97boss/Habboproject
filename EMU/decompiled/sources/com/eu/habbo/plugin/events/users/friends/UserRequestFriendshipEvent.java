package com.eu.habbo.plugin.events.users.friends;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.users.UserEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/friends/UserRequestFriendshipEvent.class */
public class UserRequestFriendshipEvent extends UserEvent {
    public final String name;
    public final Habbo friend;

    public UserRequestFriendshipEvent(Habbo habbo, String str, Habbo habbo2) {
        super(habbo);
        this.name = str;
        this.friend = habbo2;
    }
}
