package com.eu.habbo.plugin.events.navigator;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/navigator/NavigatorPopularRoomsEvent.class */
public class NavigatorPopularRoomsEvent extends NavigatorRoomsEvent {
    public final ArrayList<Room> rooms;

    public NavigatorPopularRoomsEvent(Habbo habbo, ArrayList<Room> arrayList) {
        super(habbo, arrayList);
        this.rooms = arrayList;
    }
}
