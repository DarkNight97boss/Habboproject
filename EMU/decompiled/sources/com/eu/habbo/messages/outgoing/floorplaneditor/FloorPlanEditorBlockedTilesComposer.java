package com.eu.habbo.messages.outgoing.floorplaneditor;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/floorplaneditor/FloorPlanEditorBlockedTilesComposer.class */
public class FloorPlanEditorBlockedTilesComposer extends MessageComposer {
    private final Room room;

    public FloorPlanEditorBlockedTilesComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FloorPlanEditorBlockedTilesComposer);
        THashSet<RoomTile> lockedTiles = this.room.getLockedTiles();
        this.response.appendInt(Integer.valueOf(lockedTiles.size()));
        TObjectHashIterator it = lockedTiles.iterator();
        while (it.hasNext()) {
            RoomTile roomTile = (RoomTile) it.next();
            this.response.appendInt(Integer.valueOf(roomTile.x));
            this.response.appendInt(Integer.valueOf(roomTile.y));
        }
        return this.response;
    }
}
