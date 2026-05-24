package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/handshake/MachineIDComposer.class */
public class MachineIDComposer extends MessageComposer {
    private final String machineId;

    public MachineIDComposer(String str) {
        this.machineId = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MachineIDComposer);
        this.response.appendString(this.machineId);
        return this.response;
    }
}
