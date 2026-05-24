package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/PetAction.class */
public abstract class PetAction {
    public final PetTasks petTask;
    public final boolean stopsPetWalking;
    public final List<RoomUnitStatus> statusToRemove = new ArrayList();
    public final List<RoomUnitStatus> statusToSet = new ArrayList();
    public int minimumActionDuration = 500;
    public String gestureToSet = null;

    protected PetAction(PetTasks petTasks, boolean z) {
        this.petTask = petTasks;
        this.stopsPetWalking = z;
    }

    public abstract boolean apply(Pet pet, Habbo habbo, String[] strArr);
}
