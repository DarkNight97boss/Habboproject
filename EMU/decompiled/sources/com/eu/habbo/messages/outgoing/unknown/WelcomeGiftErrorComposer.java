package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/WelcomeGiftErrorComposer.class */
public class WelcomeGiftErrorComposer extends MessageComposer {
    public static final int EMAIL_INVALID = 0;
    public static final int EMAIL_LENGTH_EXCEEDED = 1;
    public static final int EMAIL_IN_USE = 3;
    public static final int EMAIL_LIMIT_CHANGE = 4;
    private final int error;

    public WelcomeGiftErrorComposer(int i) {
        this.error = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.WelcomeGiftErrorComposer);
        this.response.appendInt(Integer.valueOf(this.error));
        return this.response;
    }
}
