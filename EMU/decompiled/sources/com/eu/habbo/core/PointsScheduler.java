package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/PointsScheduler.class */
public class PointsScheduler extends Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PointsScheduler.class);
    public static boolean IGNORE_HOTEL_VIEW;
    public static boolean IGNORE_IDLED;
    public static double HC_MODIFIER;

    public PointsScheduler() {
        super(Emulator.getConfig().getInt("hotel.auto.points.interval"));
        reloadConfig();
    }

    public void reloadConfig() {
        if (!Emulator.getConfig().getBoolean("hotel.auto.points.enabled")) {
            this.disposed = true;
            return;
        }
        IGNORE_HOTEL_VIEW = Emulator.getConfig().getBoolean("hotel.auto.points.ignore.hotelview");
        IGNORE_IDLED = Emulator.getConfig().getBoolean("hotel.auto.points.ignore.idled");
        HC_MODIFIER = Emulator.getConfig().getDouble("hotel.auto.points.hc_modifier", Double.valueOf(1.0d));
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
                        value.givePoints((int) (((double) value.getHabboInfo().getRank().getDiamondsTimerAmount()) * (value.getHabboStats().hasActiveClub() ? HC_MODIFIER : 1.0d)));
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
