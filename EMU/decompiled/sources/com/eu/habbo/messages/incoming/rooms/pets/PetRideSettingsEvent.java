package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetHorseFigureComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/PetRideSettingsEvent.class */
public class PetRideSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Pet pet;
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null && (pet = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(iIntValue)) != null && pet.getUserId() == this.client.getHabbo().getHabboInfo().getId() && (pet instanceof RideablePet)) {
            RideablePet rideablePet = (RideablePet) pet;
            rideablePet.setAnyoneCanRide(!rideablePet.anyoneCanRide());
            rideablePet.needsUpdate = true;
            if (!rideablePet.anyoneCanRide() && rideablePet.getRider() != null && rideablePet.getRider().getHabboInfo().getId() != this.client.getHabbo().getHabboInfo().getId()) {
                rideablePet.getRider().getHabboInfo().dismountPet();
            }
            if (pet instanceof HorsePet) {
                this.client.sendResponse(new RoomPetHorseFigureComposer((HorsePet) pet));
            }
        }
    }
}
