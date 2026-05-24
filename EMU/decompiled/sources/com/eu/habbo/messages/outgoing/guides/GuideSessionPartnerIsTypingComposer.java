package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideSessionPartnerIsTypingComposer.class */
public class GuideSessionPartnerIsTypingComposer extends MessageComposer {
    private final boolean typing;

    public GuideSessionPartnerIsTypingComposer(boolean z) {
        this.typing = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideSessionPartnerIsTypingComposer);
        this.response.appendBoolean(Boolean.valueOf(this.typing));
        return this.response;
    }
}
