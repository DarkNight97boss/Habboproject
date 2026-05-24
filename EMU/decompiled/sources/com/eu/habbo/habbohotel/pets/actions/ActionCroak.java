package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.threading.runnables.PetClearPosture;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionCroak.class */
public class ActionCroak extends PetAction {
    public ActionCroak() {
        super(PetTasks.SPEAK, false);
        this.minimumActionDuration = 2000;
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        pet.getRoomUnit().setStatus(RoomUnitStatus.CROAK, "0");
        Emulator.getThreading().run(new PetClearPosture(pet, RoomUnitStatus.CROAK, null, false), 2000L);
        if (pet.getHappyness() <= 80) {
            return true;
        }
        pet.say(pet.getPetData().randomVocal(PetVocalsType.PLAYFUL));
        return true;
    }
}
