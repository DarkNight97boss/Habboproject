package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/ToggleMonsterplantBreedableEvent.class */
public class ToggleMonsterplantBreedableEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Pet pet = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(this.packet.readInt().intValue());
        if (pet != null && pet.getUserId() == this.client.getHabbo().getHabboInfo().getId() && (pet instanceof MonsterplantPet)) {
            ((MonsterplantPet) pet).setPubliclyBreedable(((MonsterplantPet) pet).isPubliclyBreedable());
        }
    }
}
