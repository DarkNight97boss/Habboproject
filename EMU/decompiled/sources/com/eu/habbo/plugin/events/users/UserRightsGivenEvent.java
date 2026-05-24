package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserRightsGivenEvent.class */
public class UserRightsGivenEvent extends UserEvent {
    public final Habbo receiver;

    public UserRightsGivenEvent(Habbo habbo, Habbo habbo2) {
        super(habbo);
        this.receiver = habbo2;
    }
}
