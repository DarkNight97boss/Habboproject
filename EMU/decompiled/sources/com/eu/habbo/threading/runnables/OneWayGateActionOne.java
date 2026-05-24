package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/OneWayGateActionOne.class */
public class OneWayGateActionOne implements Runnable {
    private HabboItem oneWayGate;
    private Room room;
    private GameClient client;

    public OneWayGateActionOne(GameClient gameClient, Room room, HabboItem habboItem) {
        this.oneWayGate = habboItem;
        this.room = room;
        this.client = gameClient;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.room.sendComposer(new RoomUserStatusComposer(this.client.getHabbo().getRoomUnit()).compose());
        RoomTile tileInFront = this.room.getLayout().getTileInFront(this.room.getLayout().getTile(this.oneWayGate.getX(), this.oneWayGate.getY()), (this.oneWayGate.getRotation() + 4) % 8);
        if (this.client.getHabbo().getRoomUnit().animateWalk) {
            this.client.getHabbo().getRoomUnit().animateWalk = false;
        }
        if (tileInFront.isWalkable()) {
            if (!this.room.tileWalkable(tileInFront) || this.client.getHabbo().getRoomUnit().getX() != this.oneWayGate.getX() || this.client.getHabbo().getRoomUnit().getY() != this.oneWayGate.getY()) {
                if (this.oneWayGate.getExtradata().equals("0")) {
                    return;
                }
                Emulator.getThreading().run(new HabboItemNewState(this.oneWayGate, this.room, "0"), 1000L);
            } else {
                this.client.getHabbo().getRoomUnit().setGoalLocation(tileInFront);
                if (this.oneWayGate.getExtradata().equals("0")) {
                    return;
                }
                Emulator.getThreading().run(new HabboItemNewState(this.oneWayGate, this.room, "0"), 1000L);
            }
        }
    }
}
