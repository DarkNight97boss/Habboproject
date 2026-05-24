package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserTakeStepEvent.class */
public class UserTakeStepEvent extends UserEvent {
    public final RoomTile fromLocation;
    public final RoomTile toLocation;

    public UserTakeStepEvent(Habbo habbo, RoomTile roomTile, RoomTile roomTile2) {
        super(habbo);
        this.fromLocation = roomTile;
        this.toLocation = roomTile2;
    }
}
