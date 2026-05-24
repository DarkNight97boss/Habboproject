package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserEffectComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/SendRoomUnitEffectComposer.class */
public class SendRoomUnitEffectComposer implements Runnable {
    private final Room room;
    private final RoomUnit roomUnit;

    public SendRoomUnitEffectComposer(Room room, RoomUnit roomUnit) {
        this.room = room;
        this.roomUnit = roomUnit;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.room == null || this.roomUnit == null) {
            return;
        }
        this.room.sendComposer(new RoomUserEffectComposer(this.roomUnit).compose());
    }
}
