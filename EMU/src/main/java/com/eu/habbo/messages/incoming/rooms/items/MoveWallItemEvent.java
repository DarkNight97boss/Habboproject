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
      if (room != null) {
         if (room.hasRights(this.client.getHabbo())
            || this.client.getHabbo().hasPermission(Permission.ACC_PLACEFURNI)
            || room.getGuildId() > 0 && room.getGuildRightLevel(this.client.getHabbo()).isEqualOrGreaterThan(RoomRightLevels.GUILD_RIGHTS)) {
            int itemId = this.packet.readInt();
            String wallPosition = this.packet.readString();
            if (itemId > 0 && wallPosition.length() > 13) {
               HabboItem item = room.getHabboItem(itemId);
               if (item != null) {
                  item.setWallPosition(wallPosition);
                  item.needsUpdate(true);
                  room.updateItem(item);
               }
            }
         } else {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
         }
      }
   }
}
