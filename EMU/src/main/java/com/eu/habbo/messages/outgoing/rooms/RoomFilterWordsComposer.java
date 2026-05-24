package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;

public class RoomFilterWordsComposer extends MessageComposer {
   private final Room room;

   public RoomFilterWordsComposer(Room room) {
      this.room = room;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(2937);
      this.response.appendInt(this.room.getWordFilterWords().size());
      TObjectHashIterator var1 = this.room.getWordFilterWords().iterator();

      while (var1.hasNext()) {
         String string = (String)var1.next();
         this.response.appendString(string);
      }

      return this.response;
   }
}
