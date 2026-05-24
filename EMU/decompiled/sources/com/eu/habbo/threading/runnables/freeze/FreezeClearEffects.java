package com.eu.habbo.threading.runnables.freeze;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserEffectComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/freeze/FreezeClearEffects.class */
public class FreezeClearEffects implements Runnable {
    private final Habbo habbo;

    public FreezeClearEffects(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.habbo.getRoomUnit().setEffectId(0, 0);
        this.habbo.getRoomUnit().setCanWalk(true);
        if (this.habbo.getHabboInfo().getCurrentRoom() != null) {
            this.habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserEffectComposer(this.habbo.getRoomUnit()).compose());
        }
    }
}
