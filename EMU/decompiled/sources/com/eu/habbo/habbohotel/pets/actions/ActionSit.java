package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionSit.class */
public class ActionSit extends PetAction {
    public ActionSit() {
        super(PetTasks.SIT, true);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        if (pet.getTask() == PetTasks.SIT || pet.getRoomUnit().hasStatus(RoomUnitStatus.SIT)) {
            return false;
        }
        pet.getRoomUnit().setStatus(RoomUnitStatus.SIT, (pet.getRoom().getStackHeight(pet.getRoomUnit().getX(), pet.getRoomUnit().getY(), false) - 0.5d) + Emulator.PREVIEW);
        if (pet.getHappyness() > 75) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.PLAYFUL));
            return true;
        }
        pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_NEUTRAL));
        return true;
    }
}
