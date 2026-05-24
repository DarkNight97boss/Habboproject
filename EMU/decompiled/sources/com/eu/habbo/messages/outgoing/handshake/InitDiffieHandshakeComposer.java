package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/handshake/InitDiffieHandshakeComposer.class */
public class InitDiffieHandshakeComposer extends MessageComposer {
    private final String signedPrime;
    private final String signedGenerator;

    public InitDiffieHandshakeComposer(String str, String str2) {
        this.signedPrime = str;
        this.signedGenerator = str2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.InitDiffieHandshakeComposer);
        this.response.appendString(this.signedPrime);
        this.response.appendString(this.signedGenerator);
        return this.response;
    }
}
