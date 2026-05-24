package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.rooms.UpdateStackHeightComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PetPackageNameValidationComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/PetPackageNameEvent.class */
public class PetPackageNameEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null && (habboItem = currentRoom.getHabboItem(iIntValue)) != null && habboItem.getUserId() == this.client.getHabbo().getHabboInfo().getId()) {
            if (!string.matches("^[a-zA-Z0-9]*$")) {
                this.client.sendResponse(new PetPackageNameValidationComposer(iIntValue, 3, string.replaceAll("^[a-zA-Z0-9]*$", Emulator.PREVIEW)));
                return;
            }
            Pet petCreatePet = null;
            if (habboItem.getBaseItem().getName().equalsIgnoreCase("val11_present")) {
                petCreatePet = Emulator.getGameEnvironment().getPetManager().createPet(11, string, this.client);
            }
            if (habboItem.getBaseItem().getName().equalsIgnoreCase("gnome_box")) {
                petCreatePet = Emulator.getGameEnvironment().getPetManager().createGnome(string, currentRoom, this.client.getHabbo());
            }
            if (habboItem.getBaseItem().getName().equalsIgnoreCase("leprechaun_box")) {
                petCreatePet = Emulator.getGameEnvironment().getPetManager().createLeprechaun(string, currentRoom, this.client.getHabbo());
            }
            if (habboItem.getBaseItem().getName().equalsIgnoreCase("velociraptor_egg")) {
                petCreatePet = Emulator.getGameEnvironment().getPetManager().createPet(34, string, this.client);
            }
            if (habboItem.getBaseItem().getName().equalsIgnoreCase("pterosaur_egg")) {
                petCreatePet = Emulator.getGameEnvironment().getPetManager().createPet(33, string, this.client);
            }
            if (habboItem.getBaseItem().getName().equalsIgnoreCase("petbox_epic")) {
                petCreatePet = Emulator.getGameEnvironment().getPetManager().createPet(32, string, this.client);
            }
            if (petCreatePet != null) {
                currentRoom.placePet(petCreatePet, habboItem.getX(), habboItem.getY(), habboItem.getZ(), habboItem.getRotation());
                petCreatePet.setUserId(this.client.getHabbo().getHabboInfo().getId());
                petCreatePet.needsUpdate = true;
                petCreatePet.getRoomUnit().setLocation(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()));
                petCreatePet.getRoomUnit().setZ(habboItem.getZ());
                Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem.getId()));
                currentRoom.removeHabboItem(habboItem);
                currentRoom.sendComposer(new RemoveFloorItemComposer(habboItem).compose());
                RoomTile tile = currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY());
                currentRoom.updateTile(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()));
                currentRoom.sendComposer(new UpdateStackHeightComposer(tile.x, tile.y, tile.z, tile.relativeHeight()).compose());
                habboItem.setUserId(0);
            } else {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            }
        }
        this.client.sendResponse(new PetPackageNameValidationComposer(iIntValue, 0, Emulator.PREVIEW));
    }
}
