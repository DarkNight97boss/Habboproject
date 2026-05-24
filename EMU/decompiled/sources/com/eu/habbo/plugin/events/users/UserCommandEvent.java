package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserCommandEvent.class */
public class UserCommandEvent extends UserEvent {
    public final String[] args;
    public final boolean succes;

    public UserCommandEvent(Habbo habbo, String[] strArr, boolean z) {
        super(habbo);
        this.args = strArr;
        this.succes = z;
    }
}
