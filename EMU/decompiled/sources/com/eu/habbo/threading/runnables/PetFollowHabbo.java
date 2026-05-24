package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/PetFollowHabbo.class */
public class PetFollowHabbo implements Runnable {
    private final int directionOffset;
    private final Habbo habbo;
    private final Pet pet;

    public PetFollowHabbo(Pet pet, Habbo habbo, int i) {
        this.pet = pet;
        this.habbo = habbo;
        this.directionOffset = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.pet == null || this.pet.getTask() != PetTasks.FOLLOW || this.habbo == null || this.habbo.getRoomUnit() == null || this.pet.getRoomUnit() == null) {
            return;
        }
        RoomTile tileInFront = this.habbo.getHabboInfo().getCurrentRoom().getLayout().getTileInFront(this.habbo.getRoomUnit().getCurrentLocation(), Math.abs(((this.habbo.getRoomUnit().getBodyRotation().getValue() + this.directionOffset) + 4) % 8));
        if (tileInFront != null) {
            if (tileInFront.x < 0 || tileInFront.y < 0) {
                tileInFront = this.habbo.getHabboInfo().getCurrentRoom().getLayout().getTileInFront(this.habbo.getRoomUnit().getCurrentLocation(), this.habbo.getRoomUnit().getBodyRotation().getValue());
            }
            if (tileInFront.x >= 0 && tileInFront.y >= 0 && this.pet.getRoom().getLayout().tileWalkable(tileInFront.x, tileInFront.y)) {
                this.pet.getRoomUnit().setGoalLocation(tileInFront);
                this.pet.getRoomUnit().setCanWalk(true);
                this.pet.setTask(PetTasks.FOLLOW);
            }
            Emulator.getThreading().run(this, 500L);
        }
    }
}
