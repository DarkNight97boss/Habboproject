package com.eu.habbo.messages.outgoing.polls.infobus;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/polls/infobus/SimplePollStartComposer.class */
public class SimplePollStartComposer extends MessageComposer {
    public final int duration;
    public final String question;

    public SimplePollStartComposer(int i, String str) {
        this.duration = i;
        this.question = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.SimplePollStartComposer);
        this.response.appendString(this.question);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(this.duration));
        this.response.appendInt((Integer) (-1));
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 3);
        this.response.appendString(this.question);
        return this.response;
    }
}
