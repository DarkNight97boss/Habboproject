package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/PetLevelUpdatedComposer.class */
public class PetLevelUpdatedComposer extends MessageComposer {
    private final Pet pet;

    public PetLevelUpdatedComposer(Pet pet) {
        this.pet = pet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetLevelUpdatedComposer);
        this.response.appendInt(Integer.valueOf(this.pet.getRoomUnit().getId()));
        this.response.appendInt(Integer.valueOf(this.pet.getId()));
        this.response.appendInt(Integer.valueOf(this.pet.getLevel()));
        return this.response;
    }
}
