package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserCreditsEvent.class */
public class UserCreditsEvent extends UserEvent {
    public int credits;

    public UserCreditsEvent(Habbo habbo, int i) {
        super(habbo);
        this.credits = i;
    }
}
