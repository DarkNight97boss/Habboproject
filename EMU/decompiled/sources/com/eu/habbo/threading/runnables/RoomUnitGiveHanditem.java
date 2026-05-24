package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserHandItemComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RoomUnitGiveHanditem.class */
public class RoomUnitGiveHanditem implements Runnable {
    private final RoomUnit roomUnit;
    private final Room room;
    private final int itemId;

    public RoomUnitGiveHanditem(RoomUnit roomUnit, Room room, int i) {
        this.roomUnit = roomUnit;
        this.room = room;
        this.itemId = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.room == null || !this.roomUnit.isInRoom()) {
            return;
        }
        this.roomUnit.setHandItem(this.itemId);
        this.room.sendComposer(new RoomUserHandItemComposer(this.roomUnit).compose());
    }
}
