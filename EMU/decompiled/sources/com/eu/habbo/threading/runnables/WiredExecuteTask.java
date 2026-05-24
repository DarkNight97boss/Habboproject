package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerAtSetTime;
import com.eu.habbo.habbohotel.items.interactions.wired.triggers.WiredTriggerAtTimeLong;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/WiredExecuteTask.class */
public class WiredExecuteTask implements Runnable {
    private final InteractionWiredTrigger task;
    private final Room room;
    private int taskId;

    public WiredExecuteTask(InteractionWiredTrigger interactionWiredTrigger, Room room) {
        this.task = interactionWiredTrigger;
        this.room = room;
        if (this.task instanceof WiredTriggerAtSetTime) {
            this.taskId = ((WiredTriggerAtSetTime) this.task).taskId;
        }
        if (this.task instanceof WiredTriggerAtTimeLong) {
            this.taskId = ((WiredTriggerAtTimeLong) this.task).taskId;
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        if (Emulator.isShuttingDown || !Emulator.isReady || this.room == null || this.room.getId() != this.task.getRoomId()) {
            return;
        }
        if (!(this.task instanceof WiredTriggerAtSetTime) || ((WiredTriggerAtSetTime) this.task).taskId == this.taskId) {
            if (!(this.task instanceof WiredTriggerAtTimeLong) || ((WiredTriggerAtTimeLong) this.task).taskId == this.taskId) {
                WiredHandler.handle(this.task, (RoomUnit) null, this.room, (Object[]) null);
            }
        }
    }
}
