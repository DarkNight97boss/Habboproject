package com.eu.habbo.messages.outgoing.polls;

import com.eu.habbo.habbohotel.polls.Poll;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/polls/PollStartComposer.class */
public class PollStartComposer extends MessageComposer {
    private final Poll poll;

    public PollStartComposer(Poll poll) {
        this.poll = poll;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PollStartComposer);
        this.response.appendInt(Integer.valueOf(this.poll.id));
        this.response.appendString(this.poll.title);
        this.response.appendString(this.poll.thanksMessage);
        this.response.appendString(this.poll.title);
        return this.response;
    }
}
