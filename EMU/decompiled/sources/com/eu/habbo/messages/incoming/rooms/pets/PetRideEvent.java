package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.threading.runnables.RoomUnitRidePet;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/PetRideEvent.class */
public class PetRideEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Habbo habbo = this.client.getHabbo();
        Room currentRoom = habbo.getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        Pet pet = currentRoom.getPet(iIntValue);
        if (pet instanceof RideablePet) {
            RideablePet rideablePet = (RideablePet) pet;
            if (habbo.getHabboInfo().getRiding() != null) {
                habbo.getHabboInfo().dismountPet();
                return;
            }
            if (rideablePet.getRider() != null) {
                return;
            }
            if (rideablePet.anyoneCanRide() || habbo.getHabboInfo().getId() == rideablePet.getUserId()) {
                List<RoomTile> walkableTilesAround = currentRoom.getLayout().getWalkableTilesAround(pet.getRoomUnit().getCurrentLocation());
                if (walkableTilesAround.isEmpty()) {
                    return;
                }
                RoomTile roomTile = walkableTilesAround.get(0);
                habbo.getRoomUnit().setGoalLocation(roomTile);
                Emulator.getThreading().run(new RoomUnitRidePet(rideablePet, habbo, roomTile));
                rideablePet.getRoomUnit().setWalkTimeOut(3 + Emulator.getIntUnixTimestamp());
                rideablePet.getRoomUnit().stopWalking();
            }
        }
    }
}
