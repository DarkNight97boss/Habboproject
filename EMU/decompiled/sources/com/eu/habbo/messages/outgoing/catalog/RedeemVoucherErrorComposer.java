package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/RedeemVoucherErrorComposer.class */
public class RedeemVoucherErrorComposer extends MessageComposer {
    public static final int INVALID_CODE = 0;
    public static final int TECHNICAL_ERROR = 1;
    private final int code;

    public RedeemVoucherErrorComposer(int i) {
        this.code = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RedeemVoucherErrorComposer);
        this.response.appendString(this.code + Emulator.PREVIEW);
        return this.response;
    }
}
