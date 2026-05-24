package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolIssueUpdateComposer.class */
public class ModToolIssueUpdateComposer extends MessageComposer {
    private final ModToolIssue issue;

    public ModToolIssueUpdateComposer(ModToolIssue modToolIssue) {
        this.issue = modToolIssue;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3150);
        this.issue.serialize(this.response);
        return this.response;
    }
}
