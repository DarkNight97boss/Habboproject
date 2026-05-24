package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.threading.runnables.KickBallAction;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionPushable.class */
public abstract class InteractionPushable extends InteractionDefault {
    private KickBallAction currentThread;

    public InteractionPushable(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionPushable(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        if (this.currentThread == null || this.currentThread.dead) {
            int walkOffVelocity = getWalkOffVelocity(roomUnit, room);
            RoomUserRotation walkOffDirection = getWalkOffDirection(roomUnit, room);
            onKick(room, roomUnit, walkOffVelocity, walkOffDirection);
            if (walkOffVelocity > 0) {
                if (this.currentThread != null) {
                    this.currentThread.dead = true;
                }
                this.currentThread = new KickBallAction(this, room, roomUnit, walkOffDirection, walkOffVelocity, false);
                Emulator.getThreading().run(this.currentThread, 0L);
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
        if (gameClient != null && RoomLayout.tilesAdjecent(gameClient.getHabbo().getRoomUnit().getCurrentLocation(), room.getLayout().getTile(getX(), getY()))) {
            int tackleVelocity = getTackleVelocity(gameClient.getHabbo().getRoomUnit(), room);
            RoomUserRotation walkOnDirection = getWalkOnDirection(gameClient.getHabbo().getRoomUnit(), room);
            onTackle(room, gameClient.getHabbo().getRoomUnit(), tackleVelocity, walkOnDirection);
            if (tackleVelocity > 0) {
                if (this.currentThread != null) {
                    this.currentThread.dead = true;
                }
                this.currentThread = new KickBallAction(this, room, gameClient.getHabbo().getRoomUnit(), walkOnDirection, tackleVelocity, false);
                Emulator.getThreading().run(this.currentThread, 0L);
            }
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        int dragVelocity;
        RoomUserRotation dragDirection;
        super.onWalkOn(roomUnit, room, objArr);
        boolean z = false;
        if (getX() == roomUnit.getGoal().x && getY() == roomUnit.getGoal().y) {
            dragVelocity = getWalkOnVelocity(roomUnit, room);
            dragDirection = getWalkOnDirection(roomUnit, room);
            onKick(room, roomUnit, dragVelocity, dragDirection);
        } else {
            dragVelocity = getDragVelocity(roomUnit, room);
            dragDirection = getDragDirection(roomUnit, room);
            onDrag(room, roomUnit, dragVelocity, dragDirection);
            z = true;
        }
        if (dragVelocity > 0) {
            if (this.currentThread != null) {
                this.currentThread.dead = true;
            }
            this.currentThread = new KickBallAction(this, room, roomUnit, dragDirection, dragVelocity, z);
            Emulator.getThreading().run(this.currentThread, 0L);
        }
    }

    public abstract int getWalkOnVelocity(RoomUnit roomUnit, Room room);

    public abstract RoomUserRotation getWalkOnDirection(RoomUnit roomUnit, Room room);

    public abstract int getWalkOffVelocity(RoomUnit roomUnit, Room room);

    public abstract RoomUserRotation getWalkOffDirection(RoomUnit roomUnit, Room room);

    public abstract int getDragVelocity(RoomUnit roomUnit, Room room);

    public abstract RoomUserRotation getDragDirection(RoomUnit roomUnit, Room room);

    public abstract int getTackleVelocity(RoomUnit roomUnit, Room room);

    public abstract RoomUserRotation getTackleDirection(RoomUnit roomUnit, Room room);

    public abstract int getNextRollDelay(int i, int i2);

    public abstract RoomUserRotation getBounceDirection(Room room, RoomUserRotation roomUserRotation);

    public abstract boolean validMove(Room room, RoomTile roomTile, RoomTile roomTile2);

    public abstract void onDrag(Room room, RoomUnit roomUnit, int i, RoomUserRotation roomUserRotation);

    public abstract void onKick(Room room, RoomUnit roomUnit, int i, RoomUserRotation roomUserRotation);

    public abstract void onTackle(Room room, RoomUnit roomUnit, int i, RoomUserRotation roomUserRotation);

    public abstract void onMove(Room room, RoomTile roomTile, RoomTile roomTile2, RoomUserRotation roomUserRotation, RoomUnit roomUnit, int i, int i2, int i3);

    public abstract void onBounce(Room room, RoomUserRotation roomUserRotation, RoomUserRotation roomUserRotation2, RoomUnit roomUnit);

    public abstract void onStop(Room room, RoomUnit roomUnit, int i, int i2);

    public abstract boolean canStillMove(Room room, RoomTile roomTile, RoomTile roomTile2, RoomUserRotation roomUserRotation, RoomUnit roomUnit, int i, int i2, int i3);
}
