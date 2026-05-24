package com.eu.habbo.plugin.events.support;

import com.eu.habbo.habbohotel.modtool.ModToolBan;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/support/SupportUserBannedEvent.class */
public class SupportUserBannedEvent extends SupportEvent {
    public final Habbo target;
    public final ModToolBan ban;

    public SupportUserBannedEvent(Habbo habbo, Habbo habbo2, ModToolBan modToolBan) {
        super(habbo);
        this.target = habbo2;
        this.ban = modToolBan;
    }
}
