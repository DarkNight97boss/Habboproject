package com.eu.habbo.messages.outgoing.rooms.pets.breeding;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/breeding/PetBreedingCompleted.class */
public class PetBreedingCompleted extends MessageComposer {
    private final int type;
    private final int race;

    public PetBreedingCompleted(int i, int i2) {
        this.type = i;
        this.race = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetBreedingCompleted);
        this.response.appendInt(Integer.valueOf(this.type));
        this.response.appendInt(Integer.valueOf(this.race));
        return this.response;
    }
}
