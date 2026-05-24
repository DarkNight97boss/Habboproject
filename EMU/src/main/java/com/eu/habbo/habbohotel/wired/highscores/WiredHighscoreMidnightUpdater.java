package com.eu.habbo.habbohotel.wired.highscores;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredHighscore;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class WiredHighscoreMidnightUpdater implements Runnable {
   @Override
   public void run() {
      for (Room room : Emulator.getGameEnvironment().getRoomManager().getActiveRooms()) {
         if (room != null && room.getRoomSpecialTypes() != null) {
            THashSet<HabboItem> items = room.getRoomSpecialTypes().getItemsOfType(InteractionWiredHighscore.class);
            TObjectHashIterator var5 = items.iterator();

            while (var5.hasNext()) {
               HabboItem item = (HabboItem)var5.next();
               ((InteractionWiredHighscore)item).reloadData();
               room.updateItem(item);
            }
         }
      }

      WiredHighscoreManager.midnightUpdater = Emulator.getThreading().run(new WiredHighscoreMidnightUpdater(), getNextUpdaterRun());
   }

   public static int getNextUpdaterRun() {
      return Math.toIntExact(
            LocalDateTime.now().with(LocalTime.MIDNIGHT).plusDays(1L).plusSeconds(-1L).atZone(ZoneId.systemDefault()).toEpochSecond()
               - Emulator.getIntUnixTimestamp()
         )
         + 5;
   }
}
