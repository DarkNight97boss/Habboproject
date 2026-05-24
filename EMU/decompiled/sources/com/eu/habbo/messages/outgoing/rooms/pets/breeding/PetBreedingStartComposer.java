package com.eu.habbo.messages.outgoing.rooms.pets.breeding;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/breeding/PetBreedingStartComposer.class */
public class PetBreedingStartComposer extends MessageComposer {
    private final int state;
    private final int anInt1;
    private final int anInt2;

    public PetBreedingStartComposer(int i, int i2, int i3) {
        this.state = i;
        this.anInt1 = i2;
        this.anInt2 = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetBreedingStartComposer);
        this.response.appendInt(Integer.valueOf(this.state));
        this.response.appendInt(Integer.valueOf(this.anInt1));
        this.response.appendInt(Integer.valueOf(this.anInt2));
        return this.response;
    }
}
