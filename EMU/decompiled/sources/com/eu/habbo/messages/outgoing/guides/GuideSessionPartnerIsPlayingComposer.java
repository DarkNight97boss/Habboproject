package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideSessionPartnerIsPlayingComposer.class */
public class GuideSessionPartnerIsPlayingComposer extends MessageComposer {
    public final boolean isPlaying;

    public GuideSessionPartnerIsPlayingComposer(boolean z) {
        this.isPlaying = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideSessionPartnerIsPlayingComposer);
        this.response.appendBoolean(Boolean.valueOf(this.isPlaying));
        return this.response;
    }
}
