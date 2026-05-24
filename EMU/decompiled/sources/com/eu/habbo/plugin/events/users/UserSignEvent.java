package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserSignEvent.class */
public class UserSignEvent extends UserEvent {
    public int sign;

    public UserSignEvent(Habbo habbo, int i) {
        super(habbo);
        this.sign = i;
    }
}
