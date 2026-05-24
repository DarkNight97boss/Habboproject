package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/PetStatusUpdateComposer.class */
public class PetStatusUpdateComposer extends MessageComposer {
    private final Pet pet;

    public PetStatusUpdateComposer(Pet pet) {
        this.pet = pet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetStatusUpdateComposer);
        this.response.appendInt(Integer.valueOf(this.pet.getRoomUnit().getId()));
        this.response.appendInt(Integer.valueOf(((this.pet instanceof RideablePet) && ((RideablePet) this.pet).anyoneCanRide()) ? 1 : 0));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof MonsterplantPet) && ((MonsterplantPet) this.pet).canBreed()));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof MonsterplantPet) && !((MonsterplantPet) this.pet).isFullyGrown()));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof MonsterplantPet) && ((MonsterplantPet) this.pet).isDead()));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof MonsterplantPet) && ((MonsterplantPet) this.pet).isPubliclyBreedable()));
        return this.response;
    }
}
