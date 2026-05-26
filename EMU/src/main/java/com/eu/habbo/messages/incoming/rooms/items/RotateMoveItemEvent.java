package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemUpdateComposer;

public class RotateMoveItemEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();

        if (room == null)
            return;

        int furniId = this.packet.readInt();
        HabboItem item = room.getHabboItem(furniId);
        if (item == null) return;

        // Ownership / authority check — without this, anyone with room rights
        // (or any visitor inside a build-area edge case) could rotate/move
        // somebody else's furniture, enabling item theft and grief.
        int callerId = this.client.getHabbo().getHabboInfo().getId();
        if (item.getUserId() != callerId
                && !room.isOwner(this.client.getHabbo())
                && !this.client.getHabbo().hasPermission(Permission.ACC_MOVEROTATE)
                && !this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
            this.client.sendResponse(new FloorItemUpdateComposer(item));
            return;
        }

        int x = this.packet.readInt();
        int y = this.packet.readInt();
        int rotation = this.packet.readInt();
        RoomTile tile = room.getLayout().getTile((short) x, (short) y);

        FurnitureMovementError error = room.canPlaceFurnitureAt(item, this.client.getHabbo(), tile, rotation);
        if (!error.equals(FurnitureMovementError.NONE)) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, error.errorCode));
            this.client.sendResponse(new FloorItemUpdateComposer(item));
            return;
        }

        error = room.moveFurniTo(item, tile, rotation, this.client.getHabbo());
        if (!error.equals(FurnitureMovementError.NONE)) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, error.errorCode));
            this.client.sendResponse(new FloorItemUpdateComposer(item));
        }
    }
}