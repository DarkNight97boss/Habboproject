package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionRoller;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUnitOnRollerComposer.class */
public class RoomUnitOnRollerComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomUnitOnRollerComposer.class);
    private final RoomUnit roomUnit;
    private final HabboItem roller;
    private final RoomTile oldLocation;
    private final double oldZ;
    private final RoomTile newLocation;
    private final double newZ;
    private final Room room;
    private int x;
    private int y;
    private HabboItem oldTopItem;

    public RoomUnitOnRollerComposer(RoomUnit roomUnit, HabboItem habboItem, RoomTile roomTile, double d, RoomTile roomTile2, double d2, Room room) {
        this.roomUnit = roomUnit;
        this.roller = habboItem;
        this.oldLocation = roomTile;
        this.oldZ = d;
        this.newLocation = roomTile2;
        this.newZ = d2;
        this.room = room;
        this.oldTopItem = this.room.getTopItemAt(roomTile.x, roomTile.y);
    }

    public RoomUnitOnRollerComposer(RoomUnit roomUnit, RoomTile roomTile, Room room) {
        this.roomUnit = roomUnit;
        this.roller = null;
        this.oldLocation = this.roomUnit.getCurrentLocation();
        this.oldZ = this.roomUnit.getZ();
        this.newLocation = roomTile;
        this.newZ = this.newLocation.getStackHeight();
        this.room = room;
        this.oldTopItem = null;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        if (!this.room.isLoaded()) {
            return null;
        }
        this.response.init(3207);
        this.response.appendInt(Short.valueOf(this.oldLocation.x));
        this.response.appendInt(Short.valueOf(this.oldLocation.y));
        this.response.appendInt(Short.valueOf(this.newLocation.x));
        this.response.appendInt(Short.valueOf(this.newLocation.y));
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(this.roller == null ? 0 : this.roller.getId()));
        this.response.appendInt((Integer) 2);
        this.response.appendInt(Integer.valueOf(this.roomUnit.getId()));
        this.response.appendString(this.oldZ + Emulator.PREVIEW);
        this.response.appendString(this.newZ + Emulator.PREVIEW);
        if (this.roller == null || this.room.getLayout() == null) {
            this.roomUnit.setLocation(this.newLocation);
            this.roomUnit.setZ(this.newZ);
        } else {
            Emulator.getThreading().run(() -> {
                if (this.roomUnit.isWalking() || this.roomUnit.getCurrentLocation() != this.oldLocation) {
                    return;
                }
                HabboItem topItemAt = this.room.getTopItemAt(this.oldLocation.x, this.oldLocation.y);
                HabboItem topItemAt2 = this.room.getTopItemAt(this.newLocation.x, this.newLocation.y);
                if (topItemAt != null && (this.oldTopItem == null || this.oldTopItem != topItemAt2)) {
                    try {
                        topItemAt.onWalkOff(this.roomUnit, this.room, new Object[]{this});
                    } catch (Exception e) {
                        LOGGER.error("Caught exception", e);
                    }
                }
                this.roomUnit.setLocation(this.newLocation);
                this.roomUnit.setZ(this.newLocation.getStackHeight());
                this.roomUnit.setPreviousLocationZ(this.newLocation.getStackHeight());
                if (topItemAt2 == null || topItemAt2 == this.roller || this.oldTopItem == topItemAt2) {
                    return;
                }
                try {
                    topItemAt2.onWalkOn(this.roomUnit, this.room, new Object[]{this});
                } catch (Exception e2) {
                    LOGGER.error("Caught exception", e2);
                }
            }, this.room.getRollerSpeed() == 0 ? 250L : InteractionRoller.DELAY);
        }
        return this.response;
    }
}
