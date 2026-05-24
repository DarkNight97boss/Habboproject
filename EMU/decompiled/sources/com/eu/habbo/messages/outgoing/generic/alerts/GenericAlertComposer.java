package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/GenericAlertComposer.class */
public class GenericAlertComposer extends MessageComposer {
    private final String message;

    public GenericAlertComposer(String str) {
        this.message = str;
    }

    public GenericAlertComposer(String str, Habbo habbo) {
        this.message = str.replace("%username%", habbo.getHabboInfo().getUsername());
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GenericAlertComposer);
        this.response.appendString(this.message);
        return this.response;
    }
}
