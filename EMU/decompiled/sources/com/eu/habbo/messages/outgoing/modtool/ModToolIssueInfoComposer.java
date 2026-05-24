package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolIssueInfoComposer.class */
public class ModToolIssueInfoComposer extends MessageComposer {
    private final ModToolIssue issue;

    public ModToolIssueInfoComposer(ModToolIssue modToolIssue) {
        this.issue = modToolIssue;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolIssueInfoComposer);
        this.issue.serialize(this.response);
        return this.response;
    }
}
