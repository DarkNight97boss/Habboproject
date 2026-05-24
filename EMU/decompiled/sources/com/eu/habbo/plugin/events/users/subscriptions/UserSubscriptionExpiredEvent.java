package com.eu.habbo.plugin.events.users.subscriptions;

import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/subscriptions/UserSubscriptionExpiredEvent.class */
public class UserSubscriptionExpiredEvent extends Event {
    public final int userId;
    public final Subscription subscription;

    public UserSubscriptionExpiredEvent(int i, Subscription subscription) {
        this.userId = i;
        this.subscription = subscription;
    }
}
