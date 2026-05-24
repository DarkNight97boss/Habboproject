package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/RedeemVoucherOKComposer.class */
public class RedeemVoucherOKComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3336);
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendString(Emulator.PREVIEW);
        return this.response;
    }
}
