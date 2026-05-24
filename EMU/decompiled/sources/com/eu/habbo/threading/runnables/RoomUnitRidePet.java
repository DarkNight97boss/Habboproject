package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserEffectComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/RoomUnitRidePet.class */
public class RoomUnitRidePet implements Runnable {
    private RideablePet pet;
    private Habbo habbo;
    private RoomTile goalTile;

    public RoomUnitRidePet(RideablePet rideablePet, Habbo habbo, RoomTile roomTile) {
        this.pet = rideablePet;
        this.habbo = habbo;
        this.goalTile = roomTile;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.habbo.getRoomUnit() == null || this.pet.getRoomUnit() == null || this.pet.getRoom() != this.habbo.getHabboInfo().getCurrentRoom() || this.goalTile == null || this.habbo.getRoomUnit().getGoal() != this.goalTile) {
            return;
        }
        if (this.habbo.getRoomUnit().getCurrentLocation().distance(this.pet.getRoomUnit().getCurrentLocation()) > 1.0d) {
            this.pet.getRoomUnit().setWalkTimeOut(3 + Emulator.getIntUnixTimestamp());
            this.pet.getRoomUnit().stopWalking();
            this.habbo.getRoomUnit().setGoalLocation(this.goalTile);
            Emulator.getThreading().run(this, 500L);
            return;
        }
        this.habbo.getRoomUnit().stopWalking();
        this.habbo.getHabboInfo().getCurrentRoom().giveEffect(this.habbo, 77, -1);
        this.habbo.getHabboInfo().setRiding(this.pet);
        this.habbo.getRoomUnit().setCurrentLocation(this.pet.getRoomUnit().getCurrentLocation());
        this.habbo.getRoomUnit().setPreviousLocation(this.pet.getRoomUnit().getCurrentLocation());
        this.habbo.getRoomUnit().setZ(this.pet.getRoomUnit().getZ() + 1.0d);
        this.habbo.getRoomUnit().setPreviousLocationZ(this.pet.getRoomUnit().getZ() + 1.0d);
        this.habbo.getRoomUnit().setRotation(this.pet.getRoomUnit().getBodyRotation());
        this.habbo.getRoomUnit().statusUpdate(true);
        this.pet.setRider(this.habbo);
        this.habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserStatusComposer(this.habbo.getRoomUnit()).compose());
        this.habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserEffectComposer(this.habbo.getRoomUnit()).compose());
        this.pet.setTask(PetTasks.RIDE);
    }
}
