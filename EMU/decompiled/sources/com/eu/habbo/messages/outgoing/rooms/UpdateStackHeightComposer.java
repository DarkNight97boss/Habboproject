package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/UpdateStackHeightComposer.class */
public class UpdateStackHeightComposer extends MessageComposer {
    private int x;
    private int y;
    private short z;
    private double height;
    private THashSet<RoomTile> updateTiles;
    private Room room;

    public UpdateStackHeightComposer(int i, int i2, short s, double d) {
        this.x = i;
        this.y = i2;
        this.z = s;
        this.height = d;
    }

    public UpdateStackHeightComposer(Room room, THashSet<RoomTile> tHashSet) {
        this.updateTiles = tHashSet;
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UpdateStackHeightComposer);
        if (this.updateTiles == null) {
            this.response.appendByte(1);
            this.response.appendByte(Integer.valueOf(this.x));
            this.response.appendByte(Integer.valueOf(this.y));
            if (Emulator.getConfig().getBoolean("custom.stacking.enabled")) {
                this.response.appendShort((short) (((double) this.z) * 256.0d));
            } else {
                this.response.appendShort((int) this.height);
            }
        } else {
            if (this.updateTiles.size() > 127) {
                RoomTile[] roomTileArr = (RoomTile[]) this.updateTiles.toArray(new RoomTile[this.updateTiles.size()]);
                this.response.appendByte(127);
                for (int i = 0; i < 127; i++) {
                    RoomTile roomTile = roomTileArr[i];
                    this.updateTiles.remove(roomTile);
                    this.response.appendByte(Integer.valueOf(roomTile.x));
                    this.response.appendByte(Integer.valueOf(roomTile.y));
                    if (Emulator.getConfig().getBoolean("custom.stacking.enabled")) {
                        this.response.appendShort((short) (((double) roomTile.z) * 256.0d));
                    } else {
                        this.response.appendShort(roomTile.relativeHeight());
                    }
                }
                this.room.sendComposer(new UpdateStackHeightComposer(this.room, this.updateTiles).compose());
                return this.response;
            }
            this.response.appendByte(Integer.valueOf(this.updateTiles.size()));
            TObjectHashIterator it = this.updateTiles.iterator();
            while (it.hasNext()) {
                RoomTile roomTile2 = (RoomTile) it.next();
                this.response.appendByte(Integer.valueOf(roomTile2.x));
                this.response.appendByte(Integer.valueOf(roomTile2.y));
                if (Emulator.getConfig().getBoolean("custom.stacking.enabled")) {
                    this.response.appendShort((short) (((double) roomTile2.z) * 256.0d));
                } else {
                    this.response.appendShort(roomTile2.relativeHeight());
                }
            }
        }
        return this.response;
    }
}
