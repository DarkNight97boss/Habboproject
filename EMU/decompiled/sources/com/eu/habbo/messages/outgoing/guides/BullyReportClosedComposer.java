package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/BullyReportClosedComposer.class */
public class BullyReportClosedComposer extends MessageComposer {
    public static final int CLOSED = 1;
    public static final int MISUSE = 2;
    public final int code;

    public BullyReportClosedComposer(int i) {
        this.code = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BullyReportClosedComposer);
        this.response.appendInt(Integer.valueOf(this.code));
        return this.response;
    }
}
