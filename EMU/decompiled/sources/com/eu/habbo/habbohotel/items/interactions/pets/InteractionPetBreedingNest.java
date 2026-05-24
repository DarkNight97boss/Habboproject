package com.eu.habbo.habbohotel.items.interactions.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.pets.PetPackageNameValidationComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.breeding.PetBreedingCompleted;
import com.eu.habbo.messages.outgoing.rooms.pets.breeding.PetBreedingResultComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/pets/InteractionPetBreedingNest.class */
public class InteractionPetBreedingNest extends HabboItem {
    public Pet petOne;
    public Pet petTwo;

    public InteractionPetBreedingNest(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.petOne = null;
        this.petTwo = null;
    }

    public InteractionPetBreedingNest(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.petOne = null;
        this.petTwo = null;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return (room.getPet(roomUnit) == null || boxFull()) ? false : true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Pet pet = room.getPet(roomUnit);
        if (pet == null || boxFull()) {
            return;
        }
        addPet(pet);
        if (boxFull()) {
            Habbo habbo = room.getHabbo(this.petOne.getUserId());
            Habbo habbo2 = room.getHabbo(this.petTwo.getUserId());
            if (habbo == null || habbo2 == null || this.petOne.getPetData().getType() != this.petTwo.getPetData().getType() || this.petOne.getPetData().getOffspringType() == -1) {
                return;
            }
            habbo2.getClient().sendResponse(new PetBreedingResultComposer(getId(), this.petOne.getPetData().getOffspringType(), this.petOne, habbo.getHabboInfo().getUsername(), this.petTwo, habbo2.getHabboInfo().getUsername()));
            setExtradata("1");
            room.updateItem(this);
        }
    }

    public boolean addPet(Pet pet) {
        if (this.petOne == null) {
            this.petOne = pet;
            this.petOne.getRoomUnit().setCanWalk(false);
            return true;
        }
        if (this.petTwo != null || this.petOne == pet) {
            return false;
        }
        this.petTwo = pet;
        this.petTwo.getRoomUnit().setCanWalk(false);
        return true;
    }

    public boolean boxFull() {
        return (this.petOne == null || this.petTwo == null) ? false : true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        if (this.petOne != null && this.petOne.getRoomUnit() == roomUnit) {
            this.petOne = null;
        }
        if (this.petTwo != null && this.petTwo.getRoomUnit() == roomUnit) {
            this.petTwo = null;
        }
        setExtradata("0");
        room.updateItem(this);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }

    public void stopBreeding(Habbo habbo) {
        Habbo habbo2;
        setExtradata("0");
        habbo.getHabboInfo().getCurrentRoom().updateItem(this);
        if (this.petOne != null) {
            habbo.getClient().sendResponse(new PetPackageNameValidationComposer(getId(), 0, Emulator.PREVIEW));
        }
        if (this.petTwo.getUserId() != habbo.getHabboInfo().getId() && (habbo2 = this.petTwo.getRoom().getHabbo(this.petTwo.getUserId())) != null) {
            habbo2.getClient().sendResponse(new PetPackageNameValidationComposer(getId(), 0, Emulator.PREVIEW));
        }
        freePets();
    }

    private void freePets() {
        if (this.petOne != null) {
            this.petOne.getRoomUnit().setCanWalk(true);
            this.petOne.setTask(PetTasks.FREE);
            this.petOne = null;
        }
        if (this.petTwo != null) {
            this.petTwo.getRoomUnit().setCanWalk(true);
            this.petTwo.setTask(PetTasks.FREE);
            this.petTwo = null;
        }
    }

    public void breed(Habbo habbo, String str, int i, int i2) {
        Emulator.getThreading().run(new QueryDeleteHabboItem(getId()));
        setExtradata("2");
        habbo.getHabboInfo().getCurrentRoom().updateItem(this);
        Pet pet = this.petOne;
        Pet pet2 = this.petTwo;
        Emulator.getThreading().run(() -> {
            Pet petCreatePet = Emulator.getGameEnvironment().getPetManager().createPet(pet.getPetData().getOffspringType(), (int) Math.min(Math.round(Math.max(1.0d, PetManager.getNormalDistributionForBreeding(pet.getLevel(), pet2.getLevel()).sample())), 20L), str, habbo.getClient());
            habbo.getHabboInfo().getCurrentRoom().placePet(petCreatePet, this.getX(), this.getY(), this.getZ(), this.getRotation());
            petCreatePet.needsUpdate = true;
            petCreatePet.run();
            freePets();
            habbo.getHabboInfo().getCurrentRoom().removeHabboItem(this);
            habbo.getClient().sendResponse(new PetBreedingCompleted(petCreatePet.getId(), Emulator.getGameEnvironment().getPetManager().getRarityForOffspring(petCreatePet)));
            if (this.getBaseItem().getName().startsWith("pet_breeding_")) {
                String strReplace = this.getBaseItem().getName().replace("pet_breeding_", Emulator.PREVIEW);
                AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement(strReplace.substring(0, 1).toUpperCase() + strReplace.substring(1) + "Breeder"));
            }
        }, 2000L);
    }
}
