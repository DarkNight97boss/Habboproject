package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class BreedMonsterplantsEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        int unknownInt = this.packet.readInt(); //Something state. 2 = accept

        if (unknownInt == 0) {
            // Null-guard + ownership/rights gate to mirror ConfirmPetBreedingEvent.
            Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
            if (room == null) return;
            int callerId = this.client.getHabbo().getHabboInfo().getId();
            if (!room.hasRights(this.client.getHabbo()) && room.getOwnerId() != callerId) {
                return;
            }

            Pet petOne = room.getPet(this.packet.readInt());
            Pet petTwo = room.getPet(this.packet.readInt());

            if (petOne == null || petTwo == null || petOne == petTwo) {
                //TODO Add error
                return;
            }
            if (petOne.getUserId() != callerId || petTwo.getUserId() != callerId) {
                return;
            }

            if (petOne instanceof MonsterplantPet && petTwo instanceof MonsterplantPet) {
                ((MonsterplantPet) petOne).breed((MonsterplantPet) petTwo);
            }
        }
    }
}
