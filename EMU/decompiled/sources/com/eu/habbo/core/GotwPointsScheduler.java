package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/GotwPointsScheduler.class */
public class GotwPointsScheduler extends Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GotwPointsScheduler.class);
    public static boolean IGNORE_HOTEL_VIEW;
    public static boolean IGNORE_IDLED;
    public static String GOTW_POINTS_NAME;
    public static double HC_MODIFIER;

    public GotwPointsScheduler() {
        super(Emulator.getConfig().getInt("hotel.auto.gotwpoints.interval"));
        reloadConfig();
    }

    public void reloadConfig() {
        if (!Emulator.getConfig().getBoolean("hotel.auto.gotwpoints.enabled")) {
            this.disposed = true;
            return;
        }
        IGNORE_HOTEL_VIEW = Emulator.getConfig().getBoolean("hotel.auto.gotwpoints.ignore.hotelview");
        IGNORE_IDLED = Emulator.getConfig().getBoolean("hotel.auto.gotwpoints.ignore.idled");
        HC_MODIFIER = Emulator.getConfig().getDouble("hotel.auto.gotwpoints.hc_modifier", Double.valueOf(1.0d));
        GOTW_POINTS_NAME = Emulator.getConfig().getValue("hotel.auto.gotwpoints.name");
        if (this.disposed) {
            this.disposed = false;
            run();
        }
    }

    @Override // com.eu.habbo.core.Scheduler, java.lang.Runnable
    public void run() {
        super.run();
        Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
        while (it.hasNext()) {
            Habbo value = it.next().getValue();
            if (value != null) {
                try {
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                }
                if (value.getHabboInfo().getCurrentRoom() != null || !IGNORE_HOTEL_VIEW) {
                    if (!value.getRoomUnit().isIdle() || !IGNORE_IDLED) {
                        boolean z = false;
                        for (String str : Emulator.getConfig().getValue("seasonal.currency.names").split(";")) {
                            if (str.equalsIgnoreCase(GOTW_POINTS_NAME) || (GOTW_POINTS_NAME.startsWith(str) && Math.abs(str.length() - GOTW_POINTS_NAME.length()) < 3)) {
                                z = true;
                                break;
                            }
                        }
                        int i = Emulator.getConfig().getInt("seasonal.currency." + GOTW_POINTS_NAME, -1);
                        if (z || i != -1) {
                            value.givePoints(i, (int) (((double) value.getHabboInfo().getRank().getGotwTimerAmount()) * (value.getHabboStats().hasActiveClub() ? HC_MODIFIER : 1.0d)));
                        }
                    }
                }
            }
        }
    }

    @Override // com.eu.habbo.core.Scheduler
    public boolean isDisposed() {
        return this.disposed;
    }

    @Override // com.eu.habbo.core.Scheduler
    public void setDisposed(boolean z) {
        this.disposed = z;
    }
}
