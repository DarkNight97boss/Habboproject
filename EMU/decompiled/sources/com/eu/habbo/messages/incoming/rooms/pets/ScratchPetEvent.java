package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/ScratchPetEvent.class */
public class ScratchPetEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Pet pet;
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || (pet = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(iIntValue)) == null) {
            return;
        }
        if (this.client.getHabbo().getHabboStats().petRespectPointsToGive > 0 || (pet instanceof MonsterplantPet)) {
            pet.scratched(this.client.getHabbo());
            Emulator.getThreading().run(pet);
        }
    }
}
