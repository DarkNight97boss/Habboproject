package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/RoomPetHorseFigureComposer.class */
public class RoomPetHorseFigureComposer extends MessageComposer {
    private final HorsePet pet;

    public RoomPetHorseFigureComposer(HorsePet horsePet) {
        this.pet = horsePet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomPetHorseFigureComposer);
        this.response.appendInt(Integer.valueOf(this.pet.getRoomUnit().getId()));
        this.response.appendInt(Integer.valueOf(this.pet.getId()));
        this.response.appendInt(Integer.valueOf(this.pet.getPetData().getType()));
        this.response.appendInt(Integer.valueOf(this.pet.getRace()));
        this.response.appendString(this.pet.getColor().toLowerCase());
        if (this.pet.hasSaddle()) {
            this.response.appendInt((Integer) 2);
            this.response.appendInt((Integer) 3);
            this.response.appendInt((Integer) 4);
            this.response.appendInt((Integer) 9);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 3);
            this.response.appendInt(Integer.valueOf(this.pet.getHairStyle()));
            this.response.appendInt(Integer.valueOf(this.pet.getHairColor()));
            this.response.appendInt((Integer) 3);
            this.response.appendInt(Integer.valueOf(this.pet.getHairStyle()));
            this.response.appendInt(Integer.valueOf(this.pet.getHairColor()));
        } else {
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 2);
            this.response.appendInt((Integer) 2);
            this.response.appendInt(Integer.valueOf(this.pet.getHairStyle()));
            this.response.appendInt(Integer.valueOf(this.pet.getHairColor()));
            this.response.appendInt((Integer) 3);
            this.response.appendInt(Integer.valueOf(this.pet.getHairStyle()));
            this.response.appendInt(Integer.valueOf(this.pet.getHairColor()));
        }
        this.response.appendBoolean(Boolean.valueOf(this.pet.hasSaddle()));
        this.response.appendBoolean(false);
        return this.response;
    }
}
