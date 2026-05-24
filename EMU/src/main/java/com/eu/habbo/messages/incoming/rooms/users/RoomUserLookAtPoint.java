package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RoomUserLookAtPoint extends MessageHandler {
   @Override
   public void handle() throws Exception {
      Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
      if (room != null) {
         Habbo habbo = this.client.getHabbo();
         if (habbo.getRoomUnit().getCacheable().get("control") != null) {
            habbo = (Habbo)this.client.getHabbo().getRoomUnit().getCacheable().get("control");
            if (habbo.getHabboInfo().getCurrentRoom() != this.client.getHabbo().getHabboInfo().getCurrentRoom()) {
               habbo.getRoomUnit().getCacheable().remove("controller");
               this.client.getHabbo().getRoomUnit().getCacheable().remove("control");
               habbo = this.client.getHabbo();
            }
         }

         RoomUnit roomUnit = habbo.getRoomUnit();
         if (roomUnit.canWalk()) {
            if (!roomUnit.isWalking() && !roomUnit.hasStatus(RoomUnitStatus.MOVE)) {
               if (!roomUnit.cmdLay && !roomUnit.hasStatus(RoomUnitStatus.LAY)) {
                  if (!roomUnit.isIdle()) {
                     int x = this.packet.readInt();
                     int y = this.packet.readInt();
                     if (x != roomUnit.getX() || y != roomUnit.getY()) {
                        RoomTile tile = habbo.getHabboInfo().getCurrentRoom().getLayout().getTile((short)x, (short)y);
                        if (tile != null) {
                           roomUnit.lookAtPoint(tile);
                           roomUnit.statusUpdate(true);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
