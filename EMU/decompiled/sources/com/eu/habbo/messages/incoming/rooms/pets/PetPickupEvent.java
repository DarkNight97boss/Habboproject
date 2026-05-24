package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.AddPetComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/PetPickupEvent.class */
public class PetPickupEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Pet pet;
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (pet = currentRoom.getPet(iIntValue)) == null) {
            return;
        }
        if (this.client.getHabbo().getHabboInfo().getId() == pet.getId() || currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
            if (!this.client.getHabbo().hasPermission(Permission.ACC_UNLIMITED_PETS) && this.client.getHabbo().getInventory().getPetsComponent().getPets().size() >= PetManager.MAXIMUM_PET_INVENTORY_SIZE) {
                this.client.getHabbo().alert(Emulator.getTexts().getValue("error.pets.max.inventory").replace("%amount%", PetManager.MAXIMUM_PET_INVENTORY_SIZE + Emulator.PREVIEW));
                return;
            }
            if (pet instanceof RideablePet) {
                RideablePet rideablePet = (RideablePet) pet;
                if (rideablePet.getRider() != null) {
                    rideablePet.getRider().getHabboInfo().dismountPet(true);
                }
            }
            pet.removeFromRoom();
            Emulator.getThreading().run(pet);
            if (this.client.getHabbo().getHabboInfo().getId() == pet.getUserId()) {
                this.client.sendResponse(new AddPetComposer(pet));
                this.client.getHabbo().getInventory().getPetsComponent().addPet(pet);
                return;
            }
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(pet.getUserId());
            if (habbo != null) {
                habbo.getClient().sendResponse(new AddPetComposer(pet));
                habbo.getInventory().getPetsComponent().addPet(pet);
            }
        }
    }
}
