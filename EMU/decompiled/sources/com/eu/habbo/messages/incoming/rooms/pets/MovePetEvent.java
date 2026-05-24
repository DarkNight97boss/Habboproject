package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/MovePetEvent.class */
public class MovePetEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom;
        Pet pet = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(this.packet.readInt().intValue());
        if (pet == null || (currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom()) == null || !currentRoom.hasRights(this.client.getHabbo()) || pet.getRoomUnit() == null) {
            return;
        }
        RoomTile tile = currentRoom.getLayout().getTile((short) this.packet.readInt().intValue(), (short) this.packet.readInt().intValue());
        if (tile != null) {
            pet.getRoomUnit().setLocation(tile);
            pet.getRoomUnit().setPreviousLocation(tile);
            pet.getRoomUnit().setZ(tile.z);
            pet.getRoomUnit().setRotation(RoomUserRotation.fromValue(this.packet.readInt().intValue()));
            pet.getRoomUnit().setPreviousLocationZ(pet.getRoomUnit().getZ());
            currentRoom.sendComposer(new RoomUserStatusComposer(pet.getRoomUnit()).compose());
            pet.needsUpdate = true;
        }
    }
}
