package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserRespectedEvent.class */
public class UserRespectedEvent extends UserEvent {
    public final Habbo from;

    public UserRespectedEvent(Habbo habbo, Habbo habbo2) {
        super(habbo);
        this.from = habbo2;
    }
}
