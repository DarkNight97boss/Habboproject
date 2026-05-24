package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserNameChangedEvent.class */
public class UserNameChangedEvent extends UserEvent {
    public final String oldName;

    public UserNameChangedEvent(Habbo habbo, String str) {
        super(habbo);
        this.oldName = str;
    }
}
