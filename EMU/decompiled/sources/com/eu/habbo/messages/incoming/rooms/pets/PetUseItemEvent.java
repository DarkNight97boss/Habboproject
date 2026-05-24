package com.eu.habbo.messages.incoming.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PetStatusUpdateComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetHorseFigureComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/pets/PetUseItemEvent.class */
public class PetUseItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue)) == null) {
            return;
        }
        Pet pet = this.client.getHabbo().getHabboInfo().getCurrentRoom().getPet(this.packet.readInt().intValue());
        if (pet instanceof HorsePet) {
            if (habboItem.getBaseItem().getName().toLowerCase().startsWith("horse_dye")) {
                int iIntValue2 = Integer.valueOf(habboItem.getBaseItem().getName().split("_")[2]).intValue();
                int i = (iIntValue2 * 4) - 2;
                if (iIntValue2 >= 13 && iIntValue2 <= 17) {
                    i = ((2 + iIntValue2) * 4) + 1;
                }
                if (iIntValue2 == 0) {
                    i = 0;
                }
                pet.setRace(i);
                ((HorsePet) pet).needsUpdate = true;
            } else if (habboItem.getBaseItem().getName().toLowerCase().startsWith("horse_hairdye")) {
                int iIntValue3 = Integer.valueOf(habboItem.getBaseItem().getName().toLowerCase().split("_")[2]).intValue();
                ((HorsePet) pet).setHairColor(iIntValue3 == 0 ? -1 : iIntValue3 == 1 ? 1 : (iIntValue3 < 13 || iIntValue3 > 17) ? 48 + iIntValue3 : 68 + iIntValue3);
                ((HorsePet) pet).needsUpdate = true;
            } else if (habboItem.getBaseItem().getName().toLowerCase().startsWith("horse_hairstyle")) {
                int iIntValue4 = Integer.valueOf(habboItem.getBaseItem().getName().toLowerCase().split("_")[2]).intValue();
                ((HorsePet) pet).setHairStyle(iIntValue4 == 0 ? -1 : 100 + iIntValue4);
                ((HorsePet) pet).needsUpdate = true;
            } else if (habboItem.getBaseItem().getName().toLowerCase().startsWith("horse_saddle")) {
                ((HorsePet) pet).hasSaddle(true);
                ((HorsePet) pet).setSaddleItemId(habboItem.getBaseItem().getId());
                ((HorsePet) pet).needsUpdate = true;
            }
            if (((HorsePet) pet).needsUpdate) {
                Emulator.getThreading().run(pet);
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomPetHorseFigureComposer((HorsePet) pet).compose());
                currentRoom.removeHabboItem(habboItem);
                currentRoom.sendComposer(new RemoveFloorItemComposer(habboItem, true).compose());
                habboItem.setRoomId(0);
                Emulator.getGameEnvironment().getItemManager().deleteItem(habboItem);
                return;
            }
            return;
        }
        if (pet instanceof MonsterplantPet) {
            if (habboItem.getBaseItem().getName().equalsIgnoreCase("mnstr_revival")) {
                if (((MonsterplantPet) pet).isDead()) {
                    ((MonsterplantPet) pet).setDeathTimestamp(Emulator.getIntUnixTimestamp() + MonsterplantPet.timeToLive);
                    pet.getRoomUnit().clearStatus();
                    pet.getRoomUnit().setStatus(RoomUnitStatus.GESTURE, "rev");
                    ((MonsterplantPet) pet).packetUpdate = true;
                    this.client.getHabbo().getHabboInfo().getCurrentRoom().removeHabboItem(habboItem);
                    this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RemoveFloorItemComposer(habboItem).compose());
                    this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserStatusComposer(pet.getRoomUnit()).compose());
                    this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new PetStatusUpdateComposer(pet).compose());
                    this.client.getHabbo().getHabboInfo().getCurrentRoom().updateTiles(currentRoom.getLayout().getTilesAt(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation()));
                    AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("MonsterPlantHealer"));
                    pet.getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
                    Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem.getId()));
                    return;
                }
                return;
            }
            if (habboItem.getBaseItem().getName().equalsIgnoreCase("mnstr_fert")) {
                if (((MonsterplantPet) pet).isFullyGrown()) {
                    return;
                }
                pet.setCreated(pet.getCreated() - MonsterplantPet.growTime);
                pet.getRoomUnit().clearStatus();
                pet.cycle();
                pet.getRoomUnit().setStatus(RoomUnitStatus.GESTURE, "spd");
                pet.getRoomUnit().setStatus(RoomUnitStatus.fromString("grw" + ((MonsterplantPet) pet).getGrowthStage()), Emulator.PREVIEW);
                this.client.getHabbo().getHabboInfo().getCurrentRoom().removeHabboItem(habboItem);
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RemoveFloorItemComposer(habboItem).compose());
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserStatusComposer(pet.getRoomUnit()).compose());
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new PetStatusUpdateComposer(pet).compose());
                this.client.getHabbo().getHabboInfo().getCurrentRoom().updateTiles(currentRoom.getLayout().getTilesAt(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation()));
                pet.getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
                pet.cycle();
                Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem.getId()));
                return;
            }
            if (habboItem.getBaseItem().getName().startsWith("mnstr_rebreed") && ((MonsterplantPet) pet).isFullyGrown() && !((MonsterplantPet) pet).canBreed()) {
                if ((!habboItem.getBaseItem().getName().equalsIgnoreCase("mnstr_rebreed") || ((MonsterplantPet) pet).getRarity() > 5) && ((!habboItem.getBaseItem().getName().equalsIgnoreCase("mnstr_rebreed_2") || ((MonsterplantPet) pet).getRarity() < 6 || ((MonsterplantPet) pet).getRarity() > 8) && (!habboItem.getBaseItem().getName().equalsIgnoreCase("mnstr_rebreed_3") || ((MonsterplantPet) pet).getRarity() < 9))) {
                    return;
                }
                ((MonsterplantPet) pet).setCanBreed(true);
                pet.getRoomUnit().clearStatus();
                pet.getRoomUnit().setStatus(RoomUnitStatus.GESTURE, "reb");
                this.client.getHabbo().getHabboInfo().getCurrentRoom().removeHabboItem(habboItem);
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RemoveFloorItemComposer(habboItem).compose());
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserStatusComposer(pet.getRoomUnit()).compose());
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new PetStatusUpdateComposer(pet).compose());
                this.client.getHabbo().getHabboInfo().getCurrentRoom().updateTiles(currentRoom.getLayout().getTilesAt(currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY()), habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation()));
                pet.getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
                Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem.getId()));
            }
        }
    }
}
