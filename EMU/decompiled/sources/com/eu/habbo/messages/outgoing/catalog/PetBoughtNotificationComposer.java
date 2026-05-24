package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/PetBoughtNotificationComposer.class */
public class PetBoughtNotificationComposer extends MessageComposer {
    private final Pet pet;
    private final boolean gift;

    public PetBoughtNotificationComposer(Pet pet, boolean z) {
        this.pet = pet;
        this.gift = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetBoughtNotificationComposer);
        this.response.appendBoolean(Boolean.valueOf(this.gift));
        this.pet.serialize(this.response);
        return this.response;
    }
}
