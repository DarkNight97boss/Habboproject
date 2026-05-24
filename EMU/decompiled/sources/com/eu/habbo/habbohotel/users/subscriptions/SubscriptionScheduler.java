package com.eu.habbo.habbohotel.users.subscriptions;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.Scheduler;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.users.subscriptions.UserSubscriptionExpiredEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/subscriptions/SubscriptionScheduler.class */
public class SubscriptionScheduler extends Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionScheduler.class);

    public SubscriptionScheduler() {
        super(Emulator.getConfig().getInt("subscriptions.scheduler.interval", 10));
        reloadConfig();
    }

    public void reloadConfig() {
        if (!Emulator.getConfig().getBoolean("subscriptions.scheduler.enabled", true)) {
            this.disposed = true;
        } else if (this.disposed) {
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
                    TObjectHashIterator it2 = value.getHabboStats().subscriptions.iterator();
                    while (it2.hasNext()) {
                        Subscription subscription = (Subscription) it2.next();
                        if (subscription.isActive() && subscription.getRemaining() < 0 && !((UserSubscriptionExpiredEvent) Emulator.getPluginManager().fireEvent(new UserSubscriptionExpiredEvent(value.getHabboInfo().getId(), subscription))).isCancelled()) {
                            subscription.onExpired();
                            subscription.setActive(false);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Caught exception", e);
                }
            }
        }
        if (!SubscriptionHabboClub.HC_PAYDAY_ENABLED || SubscriptionHabboClub.isExecuting || SubscriptionHabboClub.HC_PAYDAY_NEXT_DATE >= Emulator.getIntUnixTimestamp()) {
            return;
        }
        SubscriptionHabboClub.executePayDay();
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
