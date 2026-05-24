package com.eu.habbo.messages.outgoing.rooms.pets.breeding;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/breeding/PetBreedingFailedComposer.class */
public class PetBreedingFailedComposer extends MessageComposer {
    private final int anInt1;
    private final int anInt2;

    public PetBreedingFailedComposer(int i, int i2) {
        this.anInt1 = i;
        this.anInt2 = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetBreedingFailedComposer);
        this.response.appendInt(Integer.valueOf(this.anInt1));
        this.response.appendInt(Integer.valueOf(this.anInt2));
        return this.response;
    }
}
