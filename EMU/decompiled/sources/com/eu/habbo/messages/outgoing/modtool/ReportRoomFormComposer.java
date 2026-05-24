package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ReportRoomFormComposer.class */
public class ReportRoomFormComposer extends MessageComposer {
    private final List<ModToolIssue> pendingIssues;

    public ReportRoomFormComposer(List<ModToolIssue> list) {
        this.pendingIssues = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ReportRoomFormComposer);
        this.response.appendInt(Integer.valueOf(this.pendingIssues.size()));
        for (ModToolIssue modToolIssue : this.pendingIssues) {
            this.response.appendString(modToolIssue.id + Emulator.PREVIEW);
            this.response.appendString(modToolIssue.timestamp + Emulator.PREVIEW);
            this.response.appendString(modToolIssue.message);
        }
        return this.response;
    }
}
