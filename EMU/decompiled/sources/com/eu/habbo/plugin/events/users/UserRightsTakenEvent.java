package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserRightsTakenEvent.class */
public class UserRightsTakenEvent extends UserEvent {
    public final int victimId;
    public final Habbo victim;

    public UserRightsTakenEvent(Habbo habbo, int i, Habbo habbo2) {
        super(habbo);
        this.victimId = i;
        this.victim = habbo2;
    }
}
