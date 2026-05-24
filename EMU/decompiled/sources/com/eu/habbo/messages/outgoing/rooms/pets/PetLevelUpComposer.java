package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/PetLevelUpComposer.class */
public class PetLevelUpComposer extends MessageComposer {
    private final Pet pet;

    public PetLevelUpComposer(Pet pet) {
        this.pet = pet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetLevelUpComposer);
        this.response.appendInt(Integer.valueOf(this.pet.getId()));
        this.response.appendString(this.pet.getName());
        this.response.appendInt(Integer.valueOf(this.pet.getLevel()));
        this.response.appendInt(Integer.valueOf(this.pet.getPetData().getType()));
        this.response.appendInt(Integer.valueOf(this.pet.getRace()));
        this.response.appendString(this.pet.getColor());
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
