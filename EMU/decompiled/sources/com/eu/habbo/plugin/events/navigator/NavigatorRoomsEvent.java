package com.eu.habbo.plugin.events.navigator;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.users.UserEvent;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/navigator/NavigatorRoomsEvent.class */
public abstract class NavigatorRoomsEvent extends UserEvent {
    public final ArrayList<Room> rooms;

    public NavigatorRoomsEvent(Habbo habbo, ArrayList<Room> arrayList) {
        super(habbo);
        this.rooms = arrayList;
    }
}
