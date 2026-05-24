package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionNest.class */
public class ActionNest extends PetAction {
    public ActionNest() {
        super(null, false);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        if (pet.getEnergy() >= 65) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.DISOBEY));
            return false;
        }
        pet.findNest();
        if (pet.getEnergy() >= 30) {
            return true;
        }
        pet.say(pet.getPetData().randomVocal(PetVocalsType.TIRED));
        return true;
    }
}
