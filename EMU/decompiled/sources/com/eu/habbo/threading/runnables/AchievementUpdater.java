package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/AchievementUpdater.class */
public class AchievementUpdater implements Runnable {
    public static final int INTERVAL = 300;
    public int lastExecutionTimestamp = Emulator.getIntUnixTimestamp();

    @Override // java.lang.Runnable
    public void run() {
        if (!Emulator.isShuttingDown) {
            Emulator.getThreading().run(this, 300000L);
        }
        if (Emulator.isReady) {
            Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement("AllTimeHotelPresence");
            int intUnixTimestamp = Emulator.getIntUnixTimestamp();
            Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
            while (it.hasNext()) {
                int lastOnline = 300;
                Habbo value = it.next().getValue();
                if (value.getHabboInfo().getLastOnline() > this.lastExecutionTimestamp) {
                    lastOnline = intUnixTimestamp - value.getHabboInfo().getLastOnline();
                }
                AchievementManager.progressAchievement(value, achievement, (int) Math.floor(lastOnline / 60));
            }
            this.lastExecutionTimestamp = intUnixTimestamp;
        }
    }
}
