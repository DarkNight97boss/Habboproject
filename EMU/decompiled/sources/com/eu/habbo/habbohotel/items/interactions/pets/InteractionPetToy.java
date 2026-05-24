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
import com.eu.habbo.threading.runnables.PetClearPosture;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/pets/InteractionPetToy.class */
public class InteractionPetToy extends InteractionDefault {
    public InteractionPetToy(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionPetToy(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
        Pet pet = room.getPet(roomUnit);
        if (pet == null || pet.getEnergy() <= 35) {
            return;
        }
        pet.setTask(PetTasks.PLAY);
        pet.getRoomUnit().setGoalLocation(room.getLayout().getTile(getX(), getY()));
        pet.getRoomUnit().setRotation(RoomUserRotation.values()[getRotation()]);
        pet.getRoomUnit().clearStatus();
        pet.getRoomUnit().removeStatus(RoomUnitStatus.MOVE);
        pet.getRoomUnit().setStatus(RoomUnitStatus.PLAY, "0");
        pet.packetUpdate = true;
        Emulator.getThreading().run(() -> {
            pet.addHappyness(25);
            this.setExtradata("0");
            room.updateItem(this);
            new PetClearPosture(pet, RoomUnitStatus.PLAY, null, true).run();
        }, 2500 + (Emulator.getRandom().nextInt(20) * 500));
        setExtradata("1");
        room.updateItemState(this);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOff(roomUnit, room, objArr);
        if (room.getPet(roomUnit) != null) {
            setExtradata("0");
            room.updateItemState(this);
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }
}
