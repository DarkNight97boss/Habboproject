package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionWater;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionDip.class */
public class ActionDip extends PetAction {
    public ActionDip() {
        super(null, true);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        THashSet<HabboItem> itemsOfType = pet.getRoom().getRoomSpecialTypes().getItemsOfType(InteractionWater.class);
        if (itemsOfType.isEmpty()) {
            return false;
        }
        HabboItem habboItem = (HabboItem) itemsOfType.toArray()[Emulator.getRandom().nextInt(itemsOfType.size())];
        pet.getRoomUnit().setGoalLocation(pet.getRoom().getLayout().getTile(habboItem.getX(), habboItem.getY()));
        return true;
    }
}
