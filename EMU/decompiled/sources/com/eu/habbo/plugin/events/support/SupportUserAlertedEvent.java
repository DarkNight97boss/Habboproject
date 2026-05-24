package com.eu.habbo.plugin.events.support;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/support/SupportUserAlertedEvent.class */
public class SupportUserAlertedEvent extends SupportEvent {
    public Habbo target;
    public String message;
    public SupportUserAlertedReason reason;

    public SupportUserAlertedEvent(Habbo habbo, Habbo habbo2, String str, SupportUserAlertedReason supportUserAlertedReason) {
        super(habbo);
        this.message = str;
        this.target = habbo2;
        this.reason = supportUserAlertedReason;
    }
}
