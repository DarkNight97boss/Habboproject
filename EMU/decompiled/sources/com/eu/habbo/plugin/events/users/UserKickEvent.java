package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserKickEvent.class */
public class UserKickEvent extends UserEvent {
    public final Habbo target;

    public UserKickEvent(Habbo habbo, Habbo habbo2) {
        super(habbo);
        this.target = habbo2;
    }
}
