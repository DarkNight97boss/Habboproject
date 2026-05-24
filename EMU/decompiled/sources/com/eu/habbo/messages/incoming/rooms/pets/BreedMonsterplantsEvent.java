package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/BreedMonsterplantsEvent.class */
public class BreedMonsterplantsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.packet.readInt().intValue() == 0) {
            Pet pet = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(this.packet.readInt().intValue());
            Pet pet2 = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(this.packet.readInt().intValue());
            if (pet == null || pet2 == null || pet == pet2 || !(pet instanceof MonsterplantPet) || !(pet2 instanceof MonsterplantPet)) {
                return;
            }
            ((MonsterplantPet) pet).breed((MonsterplantPet) pet2);
        }
    }
}
