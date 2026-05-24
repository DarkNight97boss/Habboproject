package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/AddPetComposer.class */
public class AddPetComposer extends MessageComposer {
    private final Pet pet;

    public AddPetComposer(Pet pet) {
        this.pet = pet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AddPetComposer);
        this.pet.serialize(this.response);
        this.response.appendBoolean(false);
        return this.response;
    }
}
