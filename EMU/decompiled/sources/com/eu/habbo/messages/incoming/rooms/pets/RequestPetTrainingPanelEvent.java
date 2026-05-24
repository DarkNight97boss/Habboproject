package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.pets.PetTrainingPanelComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/RequestPetTrainingPanelEvent.class */
public class RequestPetTrainingPanelEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Pet pet;
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || (pet = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(iIntValue)) == null) {
            return;
        }
        this.client.sendResponse(new PetTrainingPanelComposer(pet));
    }
}
