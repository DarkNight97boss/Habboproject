package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.events.support.SupportEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ScripterEvent.class */
public class ScripterEvent extends SupportEvent {
    public final Habbo habbo;
    public final String reason;

    public ScripterEvent(Habbo habbo, String str) {
        super(null);
        this.habbo = habbo;
        this.reason = str;
    }
}
