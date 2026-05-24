package com.eu.habbo.habbohotel.items.interactions.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetTasks;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.threading.runnables.PetEatAction;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/pets/InteractionPetFood.class */
public class InteractionPetFood extends InteractionDefault {
    public InteractionPetFood(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionPetFood(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        if (getExtradata().length() == 0) {
            setExtradata("0");
        }
        Pet pet = room.getPet(roomUnit);
        if (pet == null || !pet.getPetData().haveFoodItem(this) || pet.levelHunger < 35) {
            return;
        }
        pet.setTask(PetTasks.EAT);
        pet.getRoomUnit().setGoalLocation(room.getLayout().getTile(getX(), getY()));
        pet.getRoomUnit().setRotation(RoomUserRotation.values()[getRotation()]);
        pet.getRoomUnit().clearStatus();
        pet.getRoomUnit().removeStatus(RoomUnitStatus.MOVE);
        pet.getRoomUnit().setStatus(RoomUnitStatus.EAT, "0");
        room.sendComposer(new RoomUserStatusComposer(roomUnit).compose());
        Emulator.getThreading().run(new PetEatAction(pet, this));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }
}
