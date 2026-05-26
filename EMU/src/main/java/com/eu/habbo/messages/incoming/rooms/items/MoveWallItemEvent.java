package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomRightLevels;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;

public class MoveWallItemEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();

        if (room == null)
            return;

        if (!room.hasRights(this.client.getHabbo()) && !this.client.getHabbo().hasPermission(Permission.ACC_PLACEFURNI) && !(room.getGuildId() > 0 && room.getGuildRightLevel(this.client.getHabbo()).isEqualOrGreaterThan(RoomRightLevels.GUILD_RIGHTS))) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            return;
        }

        int itemId = this.packet.readInt();
        // Wall-position strings are ":wXX,YY,Z" — comfortably under 64 chars in
        // practice. Cap to defeat oversize-input memory churn (parser layer)
        // and to keep DB column bounded.
        String wallPosition = this.packet.readString(64);

        if (itemId <= 0 || wallPosition.length() <= 13 || wallPosition.length() > 64)
            return;

        // Charset whitelist: only ":wlrNN,NN,N" tokens. Anything else is
        // either a parse error from a buggy client or a hostile attempt to
        // inject control chars into the persisted column.
        if (!wallPosition.matches("^:[wl]=?[0-9,. ]+(\\s+[lr])?$")
                && !wallPosition.matches("^:[wl][0-9,. -]+$")) {
            return;
        }

        HabboItem item = room.getHabboItem(itemId);

        if (item == null)
            return;

        // Ownership gate: mirror RotateMoveItemEvent. Without this, any user
        // with room rights (or a guildmate with GUILD_RIGHTS in a public guild
        // room) can reposition wall items they don't own — vandalism / griefing
        // of personal photos and rentable ads. We allow:
        //   - the item owner
        //   - the room owner
        //   - moderators with ACC_MOVEROTATE
        int callerId = this.client.getHabbo().getHabboInfo().getId();
        boolean isItemOwner = item.getUserId() == callerId;
        boolean isRoomOwner = room.isOwner(this.client.getHabbo());
        boolean isMod = this.client.getHabbo().hasPermission(Permission.ACC_MOVEROTATE);
        if (!isItemOwner && !isRoomOwner && !isMod) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            return;
        }

        item.setWallPosition(wallPosition);
        item.needsUpdate(true);
        room.updateItem(item);
    }
}
