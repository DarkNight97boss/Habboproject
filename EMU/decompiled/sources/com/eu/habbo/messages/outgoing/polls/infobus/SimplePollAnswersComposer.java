package com.eu.habbo.messages.outgoing.polls.infobus;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/polls/infobus/SimplePollAnswersComposer.class */
public class SimplePollAnswersComposer extends MessageComposer {
    private final int no;
    private final int yes;

    public SimplePollAnswersComposer(int i, int i2) {
        this.no = i;
        this.yes = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.SimplePollAnswersComposer);
        this.response.appendInt((Integer) (-1));
        this.response.appendInt((Integer) 2);
        this.response.appendString("0");
        this.response.appendInt(Integer.valueOf(this.no));
        this.response.appendString("1");
        this.response.appendInt(Integer.valueOf(this.yes));
        return this.response;
    }
}
