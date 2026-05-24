package com.eu.habbo.plugin.events.navigator;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/navigator/NavigatorSearchResultEvent.class */
public class NavigatorSearchResultEvent extends NavigatorRoomsEvent {
    public final String prefix;
    public final String query;

    public NavigatorSearchResultEvent(Habbo habbo, String str, String str2, ArrayList<Room> arrayList) {
        super(habbo, arrayList);
        this.prefix = str;
        this.query = str2;
    }
}
