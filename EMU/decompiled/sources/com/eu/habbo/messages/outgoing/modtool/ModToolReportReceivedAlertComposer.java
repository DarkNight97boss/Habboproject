package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolReportReceivedAlertComposer.class */
public class ModToolReportReceivedAlertComposer extends MessageComposer {
    public static final int REPORT_RECEIVED = 0;
    public static final int REPORT_WINDOW = 1;
    public static final int REPORT_ABUSIVE = 2;
    private final int errorCode;
    private final String message;

    public ModToolReportReceivedAlertComposer(int i, String str) {
        this.errorCode = i;
        this.message = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolReportReceivedAlertComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendString(this.message);
        return this.response;
    }
}
