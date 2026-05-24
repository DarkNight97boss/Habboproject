package com.eu.habbo.plugin.events.sanctions;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.support.SupportEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/sanctions/SanctionEvent.class */
public class SanctionEvent extends SupportEvent {
    public Habbo target;
    public int sanctionLevel;

    public SanctionEvent(Habbo habbo, Habbo habbo2, int i) {
        super(habbo);
        this.target = habbo2;
        this.sanctionLevel = i;
    }
}
