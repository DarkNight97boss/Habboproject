package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolIssueHandledComposer.class */
public class ModToolIssueHandledComposer extends MessageComposer {
    public static final int HANDLED = 0;
    public static final int USELESS = 1;
    public static final int ABUSIVE = 2;
    private final int code;
    private final String message;

    public ModToolIssueHandledComposer(int i) {
        this.code = i;
        this.message = Emulator.PREVIEW;
    }

    public ModToolIssueHandledComposer(String str) {
        this.code = 0;
        this.message = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolIssueHandledComposer);
        this.response.appendInt(Integer.valueOf(this.code));
        this.response.appendString(this.message);
        return this.response;
    }
}
