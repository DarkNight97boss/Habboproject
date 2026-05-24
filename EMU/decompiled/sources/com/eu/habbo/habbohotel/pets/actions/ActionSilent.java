package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionSilent.class */
public class ActionSilent extends PetAction {
    public ActionSilent() {
        super(null, false);
        this.statusToRemove.add(RoomUnitStatus.SPEAK);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        pet.setMuted(true);
        pet.say(pet.getPetData().randomVocal(PetVocalsType.MUTED));
        return false;
    }
}
