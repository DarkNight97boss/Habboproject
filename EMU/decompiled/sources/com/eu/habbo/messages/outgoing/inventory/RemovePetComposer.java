package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/RemovePetComposer.class */
public class RemovePetComposer extends MessageComposer {
    private final int petId;

    public RemovePetComposer(Pet pet) {
        this.petId = pet.getId();
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RemovePetComposer);
        this.response.appendInt(Integer.valueOf(this.petId));
        return this.response;
    }
}
