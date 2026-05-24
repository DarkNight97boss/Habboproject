package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/StaffAlertAndOpenHabboWayComposer.class */
public class StaffAlertAndOpenHabboWayComposer extends MessageComposer {
    private final String message;

    public StaffAlertAndOpenHabboWayComposer(String str) {
        this.message = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.StaffAlertAndOpenHabboWayComposer);
        this.response.appendString(this.message);
        return this.response;
    }
}
