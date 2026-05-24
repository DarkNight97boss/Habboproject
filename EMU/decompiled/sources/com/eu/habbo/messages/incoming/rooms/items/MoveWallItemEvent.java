package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomRightLevels;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/MoveWallItemEvent.class */
public class MoveWallItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        if (!currentRoom.hasRights(this.client.getHabbo()) && !this.client.getHabbo().hasPermission(Permission.ACC_PLACEFURNI) && (currentRoom.getGuildId() <= 0 || !currentRoom.getGuildRightLevel(this.client.getHabbo()).isEqualOrGreaterThan(RoomRightLevels.GUILD_RIGHTS))) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        if (iIntValue <= 0 || string.length() <= 13 || (habboItem = currentRoom.getHabboItem(iIntValue)) == null) {
            return;
        }
        habboItem.setWallPosition(string);
        habboItem.needsUpdate(true);
        currentRoom.updateItem(habboItem);
    }
}
