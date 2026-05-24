package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.rooms.Room;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/WiredRepeatEffectTask.class */
class WiredRepeatEffectTask implements Runnable {
    private final InteractionWiredEffect effect;
    private final Room room;
    private final int delay;

    public WiredRepeatEffectTask(InteractionWiredEffect interactionWiredEffect, Room room, int i) {
        this.effect = interactionWiredEffect;
        this.room = room;
        this.delay = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (Emulator.isShuttingDown || !Emulator.isReady || this.room == null || this.room.getId() != this.effect.getRoomId()) {
            return;
        }
        this.effect.execute(null, this.room, null);
        Emulator.getThreading().run(this, this.delay);
    }
}
