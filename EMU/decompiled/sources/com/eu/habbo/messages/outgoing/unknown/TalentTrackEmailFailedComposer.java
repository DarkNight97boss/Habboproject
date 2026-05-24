package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/TalentTrackEmailFailedComposer.class */
public class TalentTrackEmailFailedComposer extends MessageComposer {
    private final int result;

    public TalentTrackEmailFailedComposer(int i) {
        this.result = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TalentTrackEmailFailedComposer);
        this.response.appendInt(Integer.valueOf(this.result));
        return this.response;
    }
}
