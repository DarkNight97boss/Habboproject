package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RoomUnitTeleportWalkToAction.class */
public class RoomUnitTeleportWalkToAction implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomUnitTeleportWalkToAction.class);
    private final Habbo habbo;
    private final HabboItem habboItem;
    private final Room room;

    public RoomUnitTeleportWalkToAction(Habbo habbo, HabboItem habboItem, Room room) {
        this.habbo = habbo;
        this.habboItem = habboItem;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        RoomTile squareInFront;
        if (this.habbo.getHabboInfo().getCurrentRoom() == this.room && this.habboItem.getRoomId() == this.room.getId() && (squareInFront = HabboItem.getSquareInFront(this.room.getLayout(), this.habboItem)) != null && this.habbo.getRoomUnit().getGoal().equals(squareInFront)) {
            if (this.habbo.getRoomUnit().getCurrentLocation().equals(squareInFront)) {
                try {
                    this.habboItem.onClick(this.habbo.getClient(), this.room, new Object[]{0});
                    return;
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                    return;
                }
            }
            if (squareInFront.isWalkable()) {
                this.habbo.getRoomUnit().setGoalLocation(squareInFront);
                Emulator.getThreading().run(this, this.habbo.getRoomUnit().getPath().size() + 1020);
            }
        }
    }
}
