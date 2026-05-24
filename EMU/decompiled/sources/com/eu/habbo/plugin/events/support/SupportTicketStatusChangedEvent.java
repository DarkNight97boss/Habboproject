package com.eu.habbo.plugin.events.support;

import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/support/SupportTicketStatusChangedEvent.class */
public class SupportTicketStatusChangedEvent extends SupportTicketEvent {
    public SupportTicketStatusChangedEvent(Habbo habbo, ModToolIssue modToolIssue) {
        super(habbo, modToolIssue);
    }
}
