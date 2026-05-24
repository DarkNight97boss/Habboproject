package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

public class UpdateStackHeightComposer extends MessageComposer {
   private int x;
   private int y;
   private short z;
   private double height;
   private THashSet<RoomTile> updateTiles;
   private Room room;

   public UpdateStackHeightComposer(int x, int y, short z, double height) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.height = height;
   }

   public UpdateStackHeightComposer(Room room, THashSet<RoomTile> updateTiles) {
      this.updateTiles = updateTiles;
      this.room = room;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(558);
      if (this.updateTiles != null) {
         if (this.updateTiles.size() > 127) {
            RoomTile[] tiles = (RoomTile[])this.updateTiles.toArray(new RoomTile[this.updateTiles.size()]);
            this.response.appendByte(127);

            for (int i = 0; i < 127; i++) {
               RoomTile t = tiles[i];
               this.updateTiles.remove(t);
               this.response.appendByte(Integer.valueOf(t.x));
               this.response.appendByte(Integer.valueOf(t.y));
               if (Emulator.getConfig().getBoolean("custom.stacking.enabled")) {
                  this.response.appendShort((short)(t.z * 256.0));
               } else {
                  this.response.appendShort(t.relativeHeight());
               }
            }

            this.room.sendComposer(new UpdateStackHeightComposer(this.room, this.updateTiles).compose());
            return this.response;
         }

         this.response.appendByte(this.updateTiles.size());
         TObjectHashIterator tiles = this.updateTiles.iterator();

         while (tiles.hasNext()) {
            RoomTile t = (RoomTile)tiles.next();
            this.response.appendByte(Integer.valueOf(t.x));
            this.response.appendByte(Integer.valueOf(t.y));
            if (Emulator.getConfig().getBoolean("custom.stacking.enabled")) {
               this.response.appendShort((short)(t.z * 256.0));
            } else {
               this.response.appendShort(t.relativeHeight());
            }
         }
      } else {
         this.response.appendByte(1);
         this.response.appendByte(this.x);
         this.response.appendByte(this.y);
         if (Emulator.getConfig().getBoolean("custom.stacking.enabled")) {
            this.response.appendShort((short)(this.z * 256.0));
         } else {
            this.response.appendShort((int)this.height);
         }
      }

      return this.response;
   }
}
