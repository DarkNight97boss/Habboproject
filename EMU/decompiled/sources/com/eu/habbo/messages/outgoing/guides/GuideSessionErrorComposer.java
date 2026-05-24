package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideSessionErrorComposer.class */
public class GuideSessionErrorComposer extends MessageComposer {
    public static final int SOMETHING_WRONG_REQUEST = 0;
    public static final int NO_HELPERS_AVAILABLE = 1;
    public static final int NO_GUARDIANS_AVAILABLE = 2;
    private final int errorCode;

    public GuideSessionErrorComposer(int i) {
        this.errorCode = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideSessionErrorComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        return this.response;
    }
}
