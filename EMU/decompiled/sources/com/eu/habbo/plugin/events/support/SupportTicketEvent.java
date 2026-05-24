package com.eu.habbo.plugin.events.support;

import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/support/SupportTicketEvent.class */
public class SupportTicketEvent extends SupportEvent {
    public final ModToolIssue ticket;

    public SupportTicketEvent(Habbo habbo, ModToolIssue modToolIssue) {
        super(habbo);
        this.ticket = modToolIssue;
    }
}
