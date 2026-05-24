package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserRolledEvent.class */
public class UserRolledEvent extends UserEvent {
    public final HabboItem roller;
    public final RoomTile location;

    public UserRolledEvent(Habbo habbo, HabboItem habboItem, RoomTile roomTile) {
        super(habbo);
        this.roller = habboItem;
        this.location = roomTile;
    }
}
