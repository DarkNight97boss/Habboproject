package com.eu.habbo.plugin.events.navigator;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.users.UserEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/navigator/NavigatorRoomCreatedEvent.class */
public class NavigatorRoomCreatedEvent extends UserEvent {
    public final Room room;

    public NavigatorRoomCreatedEvent(Habbo habbo, Room room) {
        super(habbo);
        this.room = room;
    }
}
