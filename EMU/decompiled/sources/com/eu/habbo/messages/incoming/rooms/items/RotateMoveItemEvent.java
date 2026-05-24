package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemUpdateComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/RotateMoveItemEvent.class */
public class RotateMoveItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (habboItem = currentRoom.getHabboItem(this.packet.readInt().intValue())) == null) {
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        RoomTile tile = currentRoom.getLayout().getTile((short) iIntValue, (short) iIntValue2);
        FurnitureMovementError furnitureMovementErrorCanPlaceFurnitureAt = currentRoom.canPlaceFurnitureAt(habboItem, this.client.getHabbo(), tile, iIntValue3);
        if (!furnitureMovementErrorCanPlaceFurnitureAt.equals(FurnitureMovementError.NONE)) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, furnitureMovementErrorCanPlaceFurnitureAt.errorCode));
            this.client.sendResponse(new FloorItemUpdateComposer(habboItem));
            return;
        }
        FurnitureMovementError furnitureMovementErrorMoveFurniTo = currentRoom.moveFurniTo(habboItem, tile, iIntValue3, this.client.getHabbo());
        if (furnitureMovementErrorMoveFurniTo.equals(FurnitureMovementError.NONE)) {
            return;
        }
        this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, furnitureMovementErrorMoveFurniTo.errorCode));
        this.client.sendResponse(new FloorItemUpdateComposer(habboItem));
    }
}
