package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.items.interactions.InteractionPushable;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionPlayFootball.class */
public class ActionPlayFootball extends PetAction {
    public ActionPlayFootball() {
        super(null, false);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        Room room = pet.getRoom();
        if (room == null || room.getLayout() == null) {
            return false;
        }
        HabboItem habboItem = null;
        TObjectHashIterator it = room.getFloorItems().iterator();
        while (it.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it.next();
            if (habboItem2 instanceof InteractionPushable) {
                habboItem = habboItem2;
            }
        }
        if (habboItem == null) {
            return false;
        }
        pet.getRoomUnit().setGoalLocation(room.getLayout().getTile(habboItem.getX(), habboItem.getY()));
        if (pet.getHappyness() > 75) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.PLAYFUL));
            return true;
        }
        pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_NEUTRAL));
        return true;
    }
}
