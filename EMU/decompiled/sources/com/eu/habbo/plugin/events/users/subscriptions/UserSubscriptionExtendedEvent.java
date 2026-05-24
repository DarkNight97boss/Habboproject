package com.eu.habbo.plugin.events.users.subscriptions;

import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/subscriptions/UserSubscriptionExtendedEvent.class */
public class UserSubscriptionExtendedEvent extends Event {
    public final int userId;
    public final Subscription subscription;
    public final int duration;

    public UserSubscriptionExtendedEvent(int i, Subscription subscription, int i2) {
        this.userId = i;
        this.subscription = subscription;
        this.duration = i2;
    }
}
