package com.eu.habbo.messages.outgoing.handshake;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/handshake/ConnectionErrorComposer.class */
public class ConnectionErrorComposer extends MessageComposer {
    private final int messageId;
    private final int errorCode;
    private final String timestamp;

    public ConnectionErrorComposer(int i) {
        this.messageId = 0;
        this.errorCode = i;
        this.timestamp = Emulator.PREVIEW;
    }

    public ConnectionErrorComposer(int i, int i2, String str) {
        this.messageId = i;
        this.errorCode = i2;
        this.timestamp = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(1004);
        this.response.appendInt(Integer.valueOf(this.messageId));
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendString(this.timestamp);
        return this.response;
    }
}
