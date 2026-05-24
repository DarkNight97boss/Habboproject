package com.eu.habbo.plugin.events.support;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/support/SupportEvent.class */
public abstract class SupportEvent extends Event {
    public Habbo moderator;

    public SupportEvent(Habbo habbo) {
        this.moderator = habbo;
    }
}
