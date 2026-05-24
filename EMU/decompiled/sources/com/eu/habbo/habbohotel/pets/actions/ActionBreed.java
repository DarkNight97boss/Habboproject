package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetBreedingNest;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.pets.breeding.PetBreedingStartFailedComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/actions/ActionBreed.class */
public class ActionBreed extends PetAction {
    public ActionBreed() {
        super(PetTasks.BREED, true);
    }

    @Override // com.eu.habbo.habbohotel.pets.PetAction
    public boolean apply(Pet pet, Habbo habbo, String[] strArr) {
        InteractionPetBreedingNest interactionPetBreedingNest = null;
        TObjectHashIterator it = pet.getRoom().getRoomSpecialTypes().getItemsOfType(InteractionPetBreedingNest.class).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            HabboItem habboItem = (HabboItem) it.next();
            if (StringUtils.containsIgnoreCase(habboItem.getBaseItem().getName(), pet.getPetData().getName()) && !((InteractionPetBreedingNest) habboItem).boxFull()) {
                interactionPetBreedingNest = (InteractionPetBreedingNest) habboItem;
                break;
            }
        }
        if (interactionPetBreedingNest != null) {
            pet.getRoomUnit().setGoalLocation(pet.getRoom().getLayout().getTile(interactionPetBreedingNest.getX(), interactionPetBreedingNest.getY()));
            return true;
        }
        habbo.getClient().sendResponse(new PetBreedingStartFailedComposer(0));
        return false;
    }
}
