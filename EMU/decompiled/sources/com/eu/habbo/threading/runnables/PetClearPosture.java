package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/PetClearPosture.class */
public class PetClearPosture implements Runnable {
    private final Pet pet;
    private final RoomUnitStatus key;
    private final PetTasks newTask;
    private final boolean clearTask;

    public PetClearPosture(Pet pet, RoomUnitStatus roomUnitStatus, PetTasks petTasks, boolean z) {
        this.pet = pet;
        this.key = roomUnitStatus;
        this.newTask = petTasks;
        this.clearTask = z;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.pet == null || this.pet.getRoom() == null || this.pet.getRoomUnit() == null) {
            return;
        }
        this.pet.getRoomUnit().removeStatus(this.key);
        if (this.clearTask) {
            this.pet.setTask(PetTasks.FREE);
        } else if (this.newTask != null) {
            this.pet.setTask(this.newTask);
        }
    }
}
