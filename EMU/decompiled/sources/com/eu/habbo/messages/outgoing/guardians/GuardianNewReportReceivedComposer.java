package com.eu.habbo.messages.outgoing.guardians;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guardians/GuardianNewReportReceivedComposer.class */
public class GuardianNewReportReceivedComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuardianNewReportReceivedComposer);
        this.response.appendInt(Integer.valueOf(Emulator.getConfig().getInt("guardians.accept.timer")));
        return this.response;
    }
}
