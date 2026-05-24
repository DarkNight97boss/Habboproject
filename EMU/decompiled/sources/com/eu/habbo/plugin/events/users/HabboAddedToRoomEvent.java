package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Collection;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/HabboAddedToRoomEvent.class */
public class HabboAddedToRoomEvent extends UserEvent {
    public final Room room;
    public Collection<Habbo> habbosToSendEnter;
    public Collection<Habbo> visibleHabbos;

    public HabboAddedToRoomEvent(Habbo habbo, Room room, Collection<Habbo> collection, Collection<Habbo> collection2) {
        super(habbo);
        this.room = room;
        this.habbosToSendEnter = collection;
        this.visibleHabbos = collection2;
    }
}
