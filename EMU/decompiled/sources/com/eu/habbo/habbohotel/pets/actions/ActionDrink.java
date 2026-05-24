package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionDrink.class */
public class ActionDrink extends PetAction {
    public ActionDrink() {
        super(null, false);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        if (pet.getLevelThirst() <= 40) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.DISOBEY));
            return false;
        }
        pet.drink();
        if (pet.getLevelThirst() <= 65) {
            return true;
        }
        pet.say(pet.getPetData().randomVocal(PetVocalsType.THIRSTY));
        return true;
    }
}
