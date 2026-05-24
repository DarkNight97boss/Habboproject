package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.threading.runnables.PetClearPosture;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionWings.class */
public class ActionWings extends PetAction {
    public ActionWings() {
        super(null, true);
        this.statusToSet.add(RoomUnitStatus.WINGS);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        Emulator.getThreading().run(new PetClearPosture(pet, RoomUnitStatus.WINGS, null, false), this.minimumActionDuration);
        if (pet.getHappyness() <= 50) {
            return true;
        }
        pet.say(pet.getPetData().randomVocal(PetVocalsType.PLAYFUL));
        return true;
    }
}
