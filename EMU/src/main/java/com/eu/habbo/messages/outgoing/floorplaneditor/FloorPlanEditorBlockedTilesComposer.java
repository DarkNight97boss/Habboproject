package com.eu.habbo.messages.outgoing.floorplaneditor;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

public class FloorPlanEditorBlockedTilesComposer extends MessageComposer {
   private final Room room;

   public FloorPlanEditorBlockedTilesComposer(Room room) {
      this.room = room;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(3990);
      THashSet<RoomTile> tileList = this.room.getLockedTiles();
      this.response.appendInt(tileList.size());
      TObjectHashIterator var2 = tileList.iterator();

      while (var2.hasNext()) {
         RoomTile node = (RoomTile)var2.next();
         this.response.appendInt(Integer.valueOf(node.x));
         this.response.appendInt(Integer.valueOf(node.y));
      }

      return this.response;
   }
}
