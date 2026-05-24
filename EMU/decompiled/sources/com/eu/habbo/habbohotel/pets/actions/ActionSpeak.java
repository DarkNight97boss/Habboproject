package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.threading.runnables.PetClearPosture;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionSpeak.class */
public class ActionSpeak extends PetAction {
    public ActionSpeak() {
        super(PetTasks.SPEAK, false);
        this.statusToSet.add(RoomUnitStatus.SPEAK);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        pet.setMuted(false);
        Emulator.getThreading().run(new PetClearPosture(pet, RoomUnitStatus.SPEAK, null, false), 2000L);
        if (pet.getHappyness() > 70) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_HAPPY));
            return true;
        }
        if (pet.getHappyness() < 30) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_SAD));
            return true;
        }
        if (pet.getLevelHunger() > 65) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.HUNGRY));
            return true;
        }
        if (pet.getLevelThirst() > 65) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.THIRSTY));
            return true;
        }
        if (pet.getEnergy() < 25) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.TIRED));
            return true;
        }
        if (pet.getTask() != PetTasks.NEST && pet.getTask() != PetTasks.DOWN) {
            return true;
        }
        pet.say(pet.getPetData().randomVocal(PetVocalsType.SLEEPING));
        return true;
    }
}
