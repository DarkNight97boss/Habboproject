package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserEnterRoomEvent.class */
public class UserEnterRoomEvent extends UserEvent {
    public final Room room;

    public UserEnterRoomEvent(Habbo habbo, Room room) {
        super(habbo);
        this.room = room;
    }
}
