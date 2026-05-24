package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/BullyReportRequestComposer.class */
public class BullyReportRequestComposer extends MessageComposer {
    public static final int START_REPORT = 0;
    public static final int ONGOING_HELPER_CASE = 1;
    public static final int INVALID_REQUESTS = 2;
    public static final int TOO_RECENT = 3;
    private final int errorCode;
    private final int errorCodeType;

    public BullyReportRequestComposer(int i, int i2) {
        this.errorCode = i;
        this.errorCodeType = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BullyReportRequestComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        if (this.errorCode == 1) {
            this.response.appendInt(Integer.valueOf(this.errorCodeType));
            this.response.appendInt((Integer) 1);
            this.response.appendBoolean(true);
            this.response.appendString("admin");
            this.response.appendString("ca-1807-64.lg-3365-78.hr-3370-42-31.hd-3093-1359.ch-3372-65");
            switch (this.errorCodeType) {
                case 1:
                    this.response.appendString("description");
                    break;
                case 3:
                    this.response.appendString("room Name");
                    break;
            }
        }
        return this.response;
    }
}
