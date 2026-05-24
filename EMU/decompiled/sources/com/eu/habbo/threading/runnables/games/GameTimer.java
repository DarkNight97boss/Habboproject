package com.eu.habbo.threading.runnables.games;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/games/GameTimer.class */
public class GameTimer implements Runnable {
    private final InteractionGameTimer timer;

    public GameTimer(InteractionGameTimer interactionGameTimer) {
        this.timer = interactionGameTimer;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.timer.getRoomId() == 0) {
            this.timer.setRunning(false);
            return;
        }
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.timer.getRoomId());
        if (room == null || !this.timer.isRunning() || this.timer.isPaused()) {
            this.timer.setThreadActive(false);
            return;
        }
        this.timer.reduceTime();
        if (this.timer.getTimeNow() < 0) {
            this.timer.setTimeNow(0);
        }
        if (this.timer.getTimeNow() > 0) {
            this.timer.setThreadActive(true);
            Emulator.getThreading().run(this, 1000L);
        } else {
            this.timer.setThreadActive(false);
            this.timer.setTimeNow(0);
            this.timer.endGame(room);
            WiredHandler.handle(WiredTriggerType.GAME_ENDS, (RoomUnit) null, room, new Object[0]);
        }
        room.updateItem(this.timer);
    }
}
