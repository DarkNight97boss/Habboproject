package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/BullyReportedMessageComposer.class */
public class BullyReportedMessageComposer extends MessageComposer {
    public static final int RECEIVED = 0;
    public static final int IGNORED = 1;
    public static final int NO_CHAT = 2;
    public static final int ALREADY_REPORTED = 3;
    public static final int NO_MISSUSE = 4;
    private final int code;

    public BullyReportedMessageComposer(int i) {
        this.code = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3285);
        this.response.appendInt(Integer.valueOf(this.code));
        return this.response;
    }
}
