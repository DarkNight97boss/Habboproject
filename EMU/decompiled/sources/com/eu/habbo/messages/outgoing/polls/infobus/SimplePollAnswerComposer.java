package com.eu.habbo.messages.outgoing.polls.infobus;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/polls/infobus/SimplePollAnswerComposer.class */
public class SimplePollAnswerComposer extends MessageComposer {
    private final int userId;
    private final String choice;
    private final int no;
    private final int yes;

    public SimplePollAnswerComposer(int i, String str, int i2, int i3) {
        this.userId = i;
        this.choice = str;
        this.no = i2;
        this.yes = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.SimplePollAnswerComposer);
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendString(this.choice);
        this.response.appendInt((Integer) 2);
        this.response.appendString("0");
        this.response.appendInt(Integer.valueOf(this.no));
        this.response.appendString("1");
        this.response.appendInt(Integer.valueOf(this.yes));
        return this.response;
    }
}
