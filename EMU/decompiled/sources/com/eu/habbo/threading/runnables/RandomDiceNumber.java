package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionColorWheel;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RandomDiceNumber.class */
public class RandomDiceNumber implements Runnable {
    private final HabboItem item;
    private final Room room;
    private final int maxNumber;
    private int result;

    public RandomDiceNumber(HabboItem habboItem, Room room, int i) {
        this.item = habboItem;
        this.room = room;
        this.maxNumber = i;
        this.result = -1;
    }

    public RandomDiceNumber(Room room, HabboItem habboItem, int i) {
        this.item = habboItem;
        this.room = room;
        this.maxNumber = -1;
        this.result = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.result <= 0) {
            this.result = Emulator.getRandom().nextInt(this.maxNumber) + 1;
        }
        this.item.setExtradata(this.result + Emulator.PREVIEW);
        this.item.needsUpdate(true);
        Emulator.getThreading().run(this.item);
        this.room.updateItem(this.item);
        if (this.item instanceof InteractionColorWheel) {
            ((InteractionColorWheel) this.item).clearRunnable();
        }
    }
}
