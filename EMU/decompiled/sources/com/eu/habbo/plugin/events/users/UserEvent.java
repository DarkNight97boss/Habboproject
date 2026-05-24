package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserEvent.class */
public abstract class UserEvent extends Event {
    public final Habbo habbo;

    public UserEvent(Habbo habbo) {
        this.habbo = habbo;
    }
}
