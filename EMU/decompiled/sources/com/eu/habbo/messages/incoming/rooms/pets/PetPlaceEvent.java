package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.PetErrorComposer;
import com.eu.habbo.messages.outgoing.inventory.RemovePetComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/PetPlaceEvent.class */
public class PetPlaceEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        RoomTile tile;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        if (this.client.getHabbo().getHabboInfo().getId() != currentRoom.getOwnerId() && !currentRoom.isAllowPets() && !this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER) && !this.client.getHabbo().hasPermission(Permission.ACC_PLACEFURNI)) {
            this.client.sendResponse(new PetErrorComposer(1));
            return;
        }
        Pet pet = this.client.getHabbo().getInventory().getPetsComponent().getPet(this.packet.readInt().intValue());
        if (pet == null) {
            return;
        }
        if (currentRoom.getCurrentPets().size() >= Room.MAXIMUM_PETS && !this.client.getHabbo().hasPermission(Permission.ACC_UNLIMITED_PETS)) {
            this.client.sendResponse(new PetErrorComposer(2));
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        RoomTile currentLocation = this.client.getHabbo().getRoomUnit().getCurrentLocation();
        if (!(iIntValue == 0 && iIntValue2 == 0) && currentRoom.isOwner(this.client.getHabbo())) {
            tile = currentRoom.getLayout().getTile((short) iIntValue, (short) iIntValue2);
        } else {
            tile = currentRoom.getLayout().getTileInFront(this.client.getHabbo().getRoomUnit().getCurrentLocation(), this.client.getHabbo().getRoomUnit().getBodyRotation().getValue());
            if (tile == null || !tile.isWalkable()) {
                this.client.sendResponse(new PetErrorComposer(4));
            }
            if (tile == null || !tile.isWalkable()) {
                tile = currentLocation;
                if (tile == null || !tile.isWalkable()) {
                    tile = currentRoom.getLayout().getDoorTile();
                }
            }
        }
        if (tile == null || !tile.isWalkable() || !tile.getAllowStack()) {
            this.client.sendResponse(new PetErrorComposer(3));
            return;
        }
        pet.setRoom(currentRoom);
        RoomUnit roomUnit = pet.getRoomUnit();
        if (roomUnit == null) {
            roomUnit = new RoomUnit();
        }
        roomUnit.setPathFinderRoom(currentRoom);
        roomUnit.setLocation(tile);
        roomUnit.setZ(tile.getStackHeight());
        roomUnit.setStatus(RoomUnitStatus.SIT, "0");
        roomUnit.setRoomUnitType(RoomUnitType.PET);
        if (currentLocation != null) {
            roomUnit.lookAtPoint(currentLocation);
        }
        pet.setRoomUnit(roomUnit);
        currentRoom.addPet(pet);
        pet.needsUpdate = true;
        Emulator.getThreading().run(pet);
        currentRoom.sendComposer(new RoomPetComposer(pet).compose());
        this.client.getHabbo().getInventory().getPetsComponent().removePet(pet);
        this.client.sendResponse(new RemovePetComposer(pet));
    }
}
