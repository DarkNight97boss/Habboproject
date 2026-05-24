package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomRelativeMapComposer.class */
public class RoomRelativeMapComposer extends MessageComposer {
    private final Room room;

    public RoomRelativeMapComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomRelativeMapComposer);
        this.response.appendInt(Integer.valueOf(this.room.getLayout().getMapSize() / this.room.getLayout().getMapSizeY()));
        this.response.appendInt(Integer.valueOf(this.room.getLayout().getMapSize()));
        short s = 0;
        while (true) {
            short s2 = s;
            if (s2 >= this.room.getLayout().getMapSizeY()) {
                return this.response;
            }
            short s3 = 0;
            while (true) {
                short s4 = s3;
                if (s4 < this.room.getLayout().getMapSizeX()) {
                    RoomTile tile = this.room.getLayout().getTile(s4, s2);
                    if (tile == null) {
                        this.response.appendShort(32767);
                    } else if (Emulator.getConfig().getBoolean("custom.stacking.enabled")) {
                        this.response.appendShort((short) (((double) tile.z) * 256.0d));
                    } else {
                        this.response.appendShort(tile.relativeHeight());
                    }
                    s3 = (short) (s4 + 1);
                }
            }
            s = (short) (s2 + 1);
        }
    }
}
