package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/BackgroundAnimation.class */
public class BackgroundAnimation implements Runnable {
    private final HabboItem toner;
    private final Room room;
    private int length = Outgoing.CraftableProductsComposer;
    private int state = 0;

    public BackgroundAnimation(HabboItem habboItem, Room room) {
        this.toner = habboItem;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (!this.room.isLoaded() || this.room.isPreLoaded()) {
            return;
        }
        this.toner.setExtradata("1:" + this.state + ":126:126");
        this.state = (this.state + 1) % 256;
        this.room.updateItem(this.toner);
        if (this.toner.getRoomId() <= 0 || this.length <= 0) {
            return;
        }
        Emulator.getThreading().run(this, 500L);
        this.length--;
    }
}
