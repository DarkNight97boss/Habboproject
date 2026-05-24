package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/RoomPetRespectComposer.class */
public class RoomPetRespectComposer extends MessageComposer {
    public static final int PET_RESPECTED = 1;
    public static final int PET_TREATED = 2;
    private final Pet pet;
    private final int type;

    public RoomPetRespectComposer(Pet pet) {
        this.pet = pet;
        this.type = 1;
    }

    public RoomPetRespectComposer(Pet pet, int i) {
        this.pet = pet;
        this.type = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomPetRespectComposer);
        this.response.appendInt(Integer.valueOf(this.type));
        this.response.appendInt((Integer) 100);
        this.pet.serialize(this.response);
        return this.response;
    }
}
