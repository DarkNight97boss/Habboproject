package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetBreedingNest;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

public class ConfirmPetBreedingEvent extends MessageHandler {

    @Override
    public void handle() throws Exception {
        int itemId = this.packet.readInt();
        String name = this.packet.readString();
        int petOneId = this.packet.readInt();
        int petTwoId = this.packet.readInt();

        // Null-guard + ownership/rights gate. Without these, any visitor could
        // force-breed a pair of pets they don't own by sending the right IDs
        // (IDOR) and the nest's `breed()` would delete the (foreign) nest furni.
        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) return;

        int callerId = this.client.getHabbo().getHabboInfo().getId();
        if (!room.hasRights(this.client.getHabbo()) && room.getOwnerId() != callerId) {
            return;
        }
        Pet petOne = room.getPet(petOneId);
        Pet petTwo = room.getPet(petTwoId);
        if (petOne == null || petTwo == null) return;
        if (petOne.getUserId() != callerId || petTwo.getUserId() != callerId) {
            return;
        }

        HabboItem item = room.getHabboItem(itemId);

        if (item instanceof InteractionPetBreedingNest) {
            ((InteractionPetBreedingNest) item).breed(this.client.getHabbo(), name, petOneId, petTwoId);
        }
    }
}