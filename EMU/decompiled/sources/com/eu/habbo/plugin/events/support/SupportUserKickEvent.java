package com.eu.habbo.plugin.events.support;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/support/SupportUserKickEvent.class */
public class SupportUserKickEvent extends SupportEvent {
    public Habbo target;
    public String message;

    public SupportUserKickEvent(Habbo habbo, Habbo habbo2, String str) {
        super(habbo);
        this.target = habbo2;
        this.message = str;
    }
}
