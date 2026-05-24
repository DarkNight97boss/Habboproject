package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionDown.class */
public class ActionDown extends PetAction {
    public ActionDown() {
        super(PetTasks.DOWN, true);
        this.statusToRemove.add(RoomUnitStatus.MOVE);
        this.statusToRemove.add(RoomUnitStatus.SIT);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        pet.getRoomUnit().setStatus(RoomUnitStatus.LAY, pet.getRoom().getStackHeight(pet.getRoomUnit().getX(), pet.getRoomUnit().getY(), false) + Emulator.PREVIEW);
        if (pet.getHappyness() > 50) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.PLAYFUL));
            return true;
        }
        pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_NEUTRAL));
        return true;
    }
}
