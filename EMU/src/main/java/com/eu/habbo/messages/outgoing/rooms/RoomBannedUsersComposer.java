package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomBan;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.NoSuchElementException;

public class RoomBannedUsersComposer extends MessageComposer {
   private final Room room;

   public RoomBannedUsersComposer(Room room) {
      this.room = room;
   }

   @Override
   protected ServerMessage composeInternal() {
      int timeStamp = Emulator.getIntUnixTimestamp();
      THashSet<RoomBan> roomBans = new THashSet();
      TIntObjectIterator<RoomBan> iterator = this.room.getBannedHabbos().iterator();
      int i = this.room.getBannedHabbos().size();

      while (i-- > 0) {
         try {
            iterator.advance();
            if (((RoomBan)iterator.value()).endTimestamp > timeStamp) {
               roomBans.add((RoomBan)iterator.value());
            }
         } catch (NoSuchElementException e) {
            break;
         }
      }

      if (roomBans.isEmpty()) {
         return null;
      }

      this.response.init(1869);
      this.response.appendInt(this.room.getId());
      this.response.appendInt(roomBans.size());
      TObjectHashIterator var7 = roomBans.iterator();

      while (var7.hasNext()) {
         RoomBan ban = (RoomBan)var7.next();
         this.response.appendInt(ban.userId);
         this.response.appendString(ban.username);
      }

      return this.response;
   }
}
