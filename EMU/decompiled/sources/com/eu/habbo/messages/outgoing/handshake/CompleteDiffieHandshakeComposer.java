package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/handshake/CompleteDiffieHandshakeComposer.class */
public class CompleteDiffieHandshakeComposer extends MessageComposer {
    private final String publicKey;
    private final boolean clientEncryption;

    public CompleteDiffieHandshakeComposer(String str) {
        this(str, true);
    }

    public CompleteDiffieHandshakeComposer(String str, boolean z) {
        this.publicKey = str;
        this.clientEncryption = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CompleteDiffieHandshakeComposer);
        this.response.appendString(this.publicKey);
        this.response.appendBoolean(Boolean.valueOf(this.clientEncryption));
        return this.response;
    }
}
