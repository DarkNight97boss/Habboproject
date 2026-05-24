package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/HabboItemNewState.class */
public class HabboItemNewState implements Runnable {
    private final HabboItem item;
    private final Room room;
    private final String state;

    public HabboItemNewState(HabboItem habboItem, Room room, String str) {
        this.item = habboItem;
        this.room = room;
        this.state = str;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.item.setExtradata(this.state);
        if (this.item.getRoomId() == this.room.getId()) {
            this.room.updateItemState(this.item);
        }
    }
}
