package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/BotFollowHabbo.class */
public class BotFollowHabbo implements Runnable {
    private final Bot bot;
    private final Habbo habbo;
    private final Room room;
    private boolean hasReached = false;

    public BotFollowHabbo(Bot bot, Habbo habbo, Room room) {
        this.bot = bot;
        this.habbo = habbo;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.bot == null || this.habbo == null || this.bot.getFollowingHabboId() != this.habbo.getHabboInfo().getId() || this.habbo.getHabboInfo().getCurrentRoom() == null || this.habbo.getHabboInfo().getCurrentRoom() != this.room || this.habbo.getRoomUnit() == null || this.bot.getRoomUnit() == null) {
            return;
        }
        RoomTile tileInFront = this.room.getLayout().getTileInFront(this.habbo.getRoomUnit().getCurrentLocation(), Math.abs(this.habbo.getRoomUnit().getBodyRotation().getValue() + 4) % 8);
        if (tileInFront != null) {
            if (tileInFront.x < 0 || tileInFront.y < 0) {
                tileInFront = this.room.getLayout().getTileInFront(this.habbo.getRoomUnit().getCurrentLocation(), this.habbo.getRoomUnit().getBodyRotation().getValue());
            }
            if (this.habbo.getRoomUnit().getCurrentLocation().distance(this.bot.getRoomUnit().getCurrentLocation()) >= 2.0d) {
                this.hasReached = false;
            } else if (!this.hasReached) {
                WiredHandler.handle(WiredTriggerType.BOT_REACHED_AVTR, this.bot.getRoomUnit(), this.room, new Object[0]);
                this.hasReached = true;
            }
            if (tileInFront.x < 0 || tileInFront.y < 0) {
                return;
            }
            this.bot.getRoomUnit().setGoalLocation(tileInFront);
            this.bot.getRoomUnit().setCanWalk(true);
            Emulator.getThreading().run(this, 500L);
        }
    }
}
