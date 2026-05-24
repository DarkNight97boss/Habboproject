package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/CantScratchPetNotOldEnoughComposer.class */
public class CantScratchPetNotOldEnoughComposer extends MessageComposer {
    private final int currentAge;
    private final int requiredAge;

    public CantScratchPetNotOldEnoughComposer(int i, int i2) {
        this.currentAge = i;
        this.requiredAge = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CantScratchPetNotOldEnoughComposer);
        this.response.appendInt(Integer.valueOf(this.currentAge));
        this.response.appendInt(Integer.valueOf(this.requiredAge));
        return this.response;
    }
}
