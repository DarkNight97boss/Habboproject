package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.habbohotel.modtool.ModToolRoomVisit;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.Calendar;
import java.util.TimeZone;

public class ModToolUserRoomVisitsComposer extends MessageComposer {
   private final HabboInfo habboInfo;
   private final THashSet<ModToolRoomVisit> roomVisits;

   public ModToolUserRoomVisitsComposer(HabboInfo habboInfo, THashSet<ModToolRoomVisit> roomVisits) {
      this.habboInfo = habboInfo;
      this.roomVisits = roomVisits;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(1752);
      this.response.appendInt(this.habboInfo.getId());
      this.response.appendString(this.habboInfo.getUsername());
      this.response.appendInt(this.roomVisits.size());
      Calendar cal = Calendar.getInstance(TimeZone.getDefault());
      TObjectHashIterator var2 = this.roomVisits.iterator();

      while (var2.hasNext()) {
         ModToolRoomVisit visit = (ModToolRoomVisit)var2.next();
         cal.setTimeInMillis(visit.timestamp * 1000);
         this.response.appendInt(visit.roomId);
         this.response.appendString(visit.roomName);
         this.response.appendInt(cal.get(10));
         this.response.appendInt(cal.get(12));
      }

      return this.response;
   }
}
