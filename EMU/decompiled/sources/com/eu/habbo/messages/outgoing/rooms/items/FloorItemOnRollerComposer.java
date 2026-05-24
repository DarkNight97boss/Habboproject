package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/FloorItemOnRollerComposer.class */
public class FloorItemOnRollerComposer extends MessageComposer {
    private final HabboItem item;
    private final HabboItem roller;
    private final RoomTile oldLocation;
    private final RoomTile newLocation;
    private final double heightOffset;
    private final double oldZ;
    private final double newZ;
    private final Room room;

    public FloorItemOnRollerComposer(HabboItem habboItem, HabboItem habboItem2, RoomTile roomTile, double d, Room room) {
        this.item = habboItem;
        this.roller = habboItem2;
        this.newLocation = roomTile;
        this.heightOffset = d;
        this.room = room;
        this.oldLocation = null;
        this.oldZ = -1.0d;
        this.newZ = -1.0d;
    }

    public FloorItemOnRollerComposer(HabboItem habboItem, HabboItem habboItem2, RoomTile roomTile, double d, RoomTile roomTile2, double d2, double d3, Room room) {
        this.item = habboItem;
        this.roller = habboItem2;
        this.oldLocation = roomTile;
        this.oldZ = d;
        this.newLocation = roomTile2;
        this.newZ = d2;
        this.heightOffset = d3;
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        short x = this.item.getX();
        short y = this.item.getY();
        this.response.init(3207);
        this.response.appendInt(Short.valueOf(this.oldLocation != null ? this.oldLocation.x : this.item.getX()));
        this.response.appendInt(Short.valueOf(this.oldLocation != null ? this.oldLocation.y : this.item.getY()));
        this.response.appendInt(Short.valueOf(this.newLocation.x));
        this.response.appendInt(Short.valueOf(this.newLocation.y));
        this.response.appendInt((Integer) 1);
        this.response.appendInt(Integer.valueOf(this.item.getId()));
        this.response.appendString(Double.toString(this.oldLocation != null ? this.oldZ : this.item.getZ()));
        this.response.appendString(Double.toString(this.oldLocation != null ? this.newZ : this.item.getZ() + this.heightOffset));
        this.response.appendInt(Integer.valueOf(this.roller != null ? this.roller.getId() : -1));
        if (this.oldLocation == null) {
            this.item.onMove(this.room, this.room.getLayout().getTile(this.item.getX(), this.item.getY()), this.newLocation);
            this.item.setX(this.newLocation.x);
            this.item.setY(this.newLocation.y);
            this.item.setZ(this.item.getZ() + this.heightOffset);
            this.item.needsUpdate(true);
            THashSet<RoomTile> tilesAt = this.room.getLayout().getTilesAt(this.room.getLayout().getTile(x, y), this.item.getBaseItem().getWidth(), this.item.getBaseItem().getLength(), this.item.getRotation());
            tilesAt.addAll(this.room.getLayout().getTilesAt(this.room.getLayout().getTile(this.item.getX(), this.item.getY()), this.item.getBaseItem().getWidth(), this.item.getBaseItem().getLength(), this.item.getRotation()));
            this.room.updateTiles(tilesAt);
        }
        return this.response;
    }
}
