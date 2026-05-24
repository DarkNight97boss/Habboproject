package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolIssueResponseAlertComposer.class */
public class ModToolIssueResponseAlertComposer extends MessageComposer {
    private final String message;

    public ModToolIssueResponseAlertComposer(String str) {
        this.message = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolIssueResponseAlertComposer);
        this.response.appendString(this.message);
        return this.response;
    }
}
