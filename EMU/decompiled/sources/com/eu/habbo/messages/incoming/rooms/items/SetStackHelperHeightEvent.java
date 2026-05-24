package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionStackHelper;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.UpdateStackHeightComposer;
import com.eu.habbo.messages.outgoing.rooms.items.UpdateStackHeightTileHeightComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/SetStackHelperHeightEvent.class */
public class SetStackHelperHeightEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return;
        }
        if (this.client.getHabbo().getHabboInfo().getId() == this.client.getHabbo().getHabboInfo().getCurrentRoom().getOwnerId() || this.client.getHabbo().getHabboInfo().getCurrentRoom().hasRights(this.client.getHabbo())) {
            HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue);
            if (habboItem instanceof InteractionStackHelper) {
                Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
                RoomTile tile = currentRoom.getLayout().getTile(habboItem.getX(), habboItem.getY());
                double dIntValue = this.packet.readInt().intValue();
                THashSet<RoomTile> tilesAt = currentRoom.getLayout().getTilesAt(tile, habboItem.getBaseItem().getWidth(), habboItem.getBaseItem().getLength(), habboItem.getRotation());
                if (dIntValue == -100.0d) {
                    TObjectHashIterator it = tilesAt.iterator();
                    while (it.hasNext()) {
                        RoomTile roomTile = (RoomTile) it.next();
                        double stackHeight = currentRoom.getStackHeight(roomTile.x, roomTile.y, false, habboItem) * 100.0d;
                        if (stackHeight > dIntValue) {
                            dIntValue = stackHeight;
                        }
                    }
                } else {
                    dIntValue = Math.min(Math.max(dIntValue, tile.z * 100), Room.MAXIMUM_FURNI_HEIGHT * 100.0d);
                }
                double d = 0.0d;
                if (dIntValue >= 0.0d) {
                    d = dIntValue / 100.0d;
                }
                TObjectHashIterator it2 = tilesAt.iterator();
                while (it2.hasNext()) {
                    ((RoomTile) it2.next()).setStackHeight(d);
                }
                habboItem.setZ(d);
                habboItem.setExtradata(((int) (d * 100.0d)) + Emulator.PREVIEW);
                habboItem.needsUpdate(true);
                this.client.getHabbo().getHabboInfo().getCurrentRoom().updateItem(habboItem);
                this.client.getHabbo().getHabboInfo().getCurrentRoom().updateTiles(tilesAt);
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new UpdateStackHeightComposer(currentRoom, tilesAt).compose());
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new UpdateStackHeightTileHeightComposer(habboItem, (int) (d * 100.0d)).compose());
            }
        }
    }
}
