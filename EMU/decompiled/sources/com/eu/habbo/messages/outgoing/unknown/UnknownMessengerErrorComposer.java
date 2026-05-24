package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownMessengerErrorComposer.class */
public class UnknownMessengerErrorComposer extends MessageComposer {
    private final int errorCode;
    private final int userId;
    private final String message;

    public UnknownMessengerErrorComposer(int i, int i2, String str) {
        this.errorCode = i;
        this.userId = i2;
        this.message = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownMessengerErrorComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendString(this.message);
        return this.response;
    }
}
