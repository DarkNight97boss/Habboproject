package com.eu.habbo.habbohotel.items.interactions.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.threading.runnables.PetClearPosture;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/pets/InteractionPetDrink.class */
public class InteractionPetDrink extends InteractionDefault {
    public InteractionPetDrink(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionPetDrink(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        Pet pet = room.getPet(roomUnit);
        if (pet == null || !pet.getPetData().haveDrinkItem(this) || pet.levelThirst < 35) {
            return;
        }
        pet.setTask(PetTasks.EAT);
        pet.getRoomUnit().setGoalLocation(room.getLayout().getTile(getX(), getY()));
        pet.getRoomUnit().setRotation(RoomUserRotation.values()[getRotation()]);
        pet.getRoomUnit().clearStatus();
        pet.getRoomUnit().removeStatus(RoomUnitStatus.MOVE);
        pet.getRoomUnit().setStatus(RoomUnitStatus.EAT, "0");
        pet.addThirst(-75);
        room.sendComposer(new RoomUserStatusComposer(roomUnit).compose());
        Emulator.getThreading().run(new PetClearPosture(pet, RoomUnitStatus.EAT, null, true), 500L);
        AchievementManager.progressAchievement(Emulator.getGameEnvironment().getHabboManager().getHabbo(pet.getUserId()), Emulator.getGameEnvironment().getAchievementManager().getAchievement("PetFeeding"), 75);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }
}
