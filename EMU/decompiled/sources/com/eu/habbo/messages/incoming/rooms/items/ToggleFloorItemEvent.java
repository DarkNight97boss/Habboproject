package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionDice;
import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionMonsterPlantSeed;
import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PetPackageComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.plugin.events.furniture.FurnitureToggleEvent;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/ToggleFloorItemEvent.class */
public class ToggleFloorItemEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToggleFloorItemEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        try {
            Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
            if (currentRoom == null) {
                return;
            }
            int iIntValue = this.packet.readInt().intValue();
            int iIntValue2 = this.packet.readInt().intValue();
            HabboItem habboItem = currentRoom.getHabboItem(iIntValue);
            if (habboItem == null || (habboItem instanceof InteractionDice)) {
                return;
            }
            FurnitureToggleEvent furnitureToggleEvent = new FurnitureToggleEvent(habboItem, this.client.getHabbo(), iIntValue2);
            Emulator.getPluginManager().fireEvent(furnitureToggleEvent);
            if (furnitureToggleEvent.isCancelled()) {
                return;
            }
            if (!(habboItem instanceof InteractionMonsterPlantSeed)) {
                if ((habboItem.getBaseItem().getName().equalsIgnoreCase("val11_present") || habboItem.getBaseItem().getName().equalsIgnoreCase("gnome_box") || habboItem.getBaseItem().getName().equalsIgnoreCase("leprechaun_box") || habboItem.getBaseItem().getName().equalsIgnoreCase("velociraptor_egg") || habboItem.getBaseItem().getName().equalsIgnoreCase("pterosaur_egg") || habboItem.getBaseItem().getName().equalsIgnoreCase("petbox_epic")) && currentRoom.getCurrentPets().size() < Room.MAXIMUM_PETS) {
                    this.client.sendResponse(new PetPackageComposer(habboItem));
                    return;
                }
                habboItem.onClick(this.client, currentRoom, new Object[]{Integer.valueOf(iIntValue2)});
                if (habboItem instanceof InteractionWired) {
                    this.client.getHabbo().getRoomUnit().setGoalLocation(this.client.getHabbo().getRoomUnit().getCurrentLocation());
                }
                return;
            }
            Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem.getId()));
            int iIntValue3 = 0;
            boolean zContains = habboItem.getBaseItem().getName().contains("rare");
            if ((habboItem.getExtradata().isEmpty() || Integer.valueOf(habboItem.getExtradata()).intValue() - 1 >= 0) && !habboItem.getExtradata().isEmpty()) {
                try {
                    iIntValue3 = Integer.valueOf(habboItem.getExtradata()).intValue() - 1;
                } catch (Exception e) {
                }
            } else {
                iIntValue3 = zContains ? InteractionMonsterPlantSeed.randomGoldenRarityLevel() : InteractionMonsterPlantSeed.randomRarityLevel();
            }
            MonsterplantPet monsterplantPetCreateMonsterplant = Emulator.getGameEnvironment().getPetManager().createMonsterplant(currentRoom, this.client.getHabbo(), zContains, currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()), iIntValue3);
            currentRoom.sendComposer(new RemoveFloorItemComposer(habboItem, true).compose());
            currentRoom.removeHabboItem(habboItem);
            currentRoom.updateTile(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()));
            currentRoom.placePet(monsterplantPetCreateMonsterplant, habboItem.getX(), habboItem.getY(), habboItem.getZ(), habboItem.getRotation());
            monsterplantPetCreateMonsterplant.cycle();
            currentRoom.sendComposer(new RoomUserStatusComposer(monsterplantPetCreateMonsterplant.getRoomUnit()).compose());
        } catch (Exception e2) {
            LOGGER.error("Caught exception", e2);
        }
    }
}
