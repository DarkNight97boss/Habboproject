package com.eu.habbo.plugin.events.support;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/support/SupportRoomActionEvent.class */
public class SupportRoomActionEvent extends SupportEvent {
    public final Room room;
    public boolean kickUsers;
    public boolean lockDoor;
    public boolean changeTitle;

    public SupportRoomActionEvent(Habbo habbo, Room room, boolean z, boolean z2, boolean z3) {
        super(habbo);
        this.room = room;
        this.kickUsers = z;
        this.lockDoor = z2;
        this.changeTitle = z3;
    }
}
