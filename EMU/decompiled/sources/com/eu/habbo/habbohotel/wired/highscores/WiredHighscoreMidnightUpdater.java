package com.eu.habbo.habbohotel.wired.highscores;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredHighscore;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjuster;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/highscores/WiredHighscoreMidnightUpdater.class */
public class WiredHighscoreMidnightUpdater implements Runnable {
    @Override // java.lang.Runnable
    public void run() {
        for (Room room : Emulator.getGameEnvironment().getRoomManager().getActiveRooms()) {
            if (room != null && room.getRoomSpecialTypes() != null) {
                TObjectHashIterator it = room.getRoomSpecialTypes().getItemsOfType(InteractionWiredHighscore.class).iterator();
                while (it.hasNext()) {
                    HabboItem habboItem = (HabboItem) it.next();
                    ((InteractionWiredHighscore) habboItem).reloadData();
                    room.updateItem(habboItem);
                }
            }
        }
        WiredHighscoreManager.midnightUpdater = Emulator.getThreading().run(new WiredHighscoreMidnightUpdater(), getNextUpdaterRun());
    }

    /* JADX WARN: Type inference failed for: r0v4, types: [java.time.ZonedDateTime] */
    public static int getNextUpdaterRun() {
        return Math.toIntExact(LocalDateTime.now().with((TemporalAdjuster) LocalTime.MIDNIGHT).plusDays(1L).plusSeconds(-1L).atZone(ZoneId.systemDefault()).toEpochSecond() - ((long) Emulator.getIntUnixTimestamp())) + 5;
    }
}
