package com.eu.habbo.messages.outgoing.events.resolution;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/events/resolution/NewYearResolutionCompletedComposer.class */
public class NewYearResolutionCompletedComposer extends MessageComposer {
    public final String badge;

    public NewYearResolutionCompletedComposer(String str) {
        this.badge = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.NewYearResolutionCompletedComposer);
        this.response.appendString(this.badge);
        this.response.appendString(this.badge);
        return this.response;
    }
}
