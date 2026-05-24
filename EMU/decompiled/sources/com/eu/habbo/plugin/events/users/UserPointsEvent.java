package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserPointsEvent.class */
public class UserPointsEvent extends UserEvent {
    public int points;
    public int type;

    public UserPointsEvent(Habbo habbo, int i, int i2) {
        super(habbo);
        this.points = i;
        this.type = i2;
    }
}
