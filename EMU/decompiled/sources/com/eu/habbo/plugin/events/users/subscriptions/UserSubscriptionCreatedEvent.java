package com.eu.habbo.plugin.events.users.subscriptions;

import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/subscriptions/UserSubscriptionCreatedEvent.class */
public class UserSubscriptionCreatedEvent extends Event {
    public final int userId;
    public final String subscriptionType;
    public final int duration;

    public UserSubscriptionCreatedEvent(int i, String str, int i2) {
        this.userId = i;
        this.subscriptionType = str;
        this.duration = i2;
    }
}
