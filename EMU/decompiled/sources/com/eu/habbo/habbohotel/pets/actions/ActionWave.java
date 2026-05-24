package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.threading.runnables.PetClearPosture;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionWave.class */
public class ActionWave extends PetAction {
    public ActionWave() {
        super(PetTasks.WAVE, false);
        this.statusToSet.add(RoomUnitStatus.WAVE);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        if (pet.getHappyness() <= 65) {
            return false;
        }
        pet.getRoomUnit().setStatus(RoomUnitStatus.WAVE, "0");
        Emulator.getThreading().run(new PetClearPosture(pet, RoomUnitStatus.WAVE, null, false), 2000L);
        return true;
    }
}
