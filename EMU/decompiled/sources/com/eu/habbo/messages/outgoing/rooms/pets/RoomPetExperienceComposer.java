package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/RoomPetExperienceComposer.class */
public class RoomPetExperienceComposer extends MessageComposer {
    private final Pet pet;
    private final int amount;

    public RoomPetExperienceComposer(Pet pet, int i) {
        this.pet = pet;
        this.amount = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomPetExperienceComposer);
        this.response.appendInt(Integer.valueOf(this.pet.getId()));
        this.response.appendInt(Integer.valueOf(this.pet.getRoomUnit().getId()));
        this.response.appendInt(Integer.valueOf(this.amount));
        return this.response;
    }
}
