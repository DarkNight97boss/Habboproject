package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/StaffAlertWIthLinkAndOpenHabboWayComposer.class */
public class StaffAlertWIthLinkAndOpenHabboWayComposer extends MessageComposer {
    private final String message;
    private final String link;

    public StaffAlertWIthLinkAndOpenHabboWayComposer(String str, String str2) {
        this.message = str;
        this.link = str2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.StaffAlertWIthLinkAndOpenHabboWayComposer);
        this.response.appendString(this.message);
        this.response.appendString(this.link);
        return this.response;
    }
}
