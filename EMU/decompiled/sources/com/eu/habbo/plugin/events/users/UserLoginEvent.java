package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserLoginEvent.class */
public class UserLoginEvent extends UserEvent {
    public final String ip;

    public UserLoginEvent(Habbo habbo, String str) {
        super(habbo);
        this.ip = str;
    }
}
