package com.eu.habbo.messages.incoming.polls;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.polls.Poll;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.polls.PollQuestionsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/polls/GetPollDataEvent.class */
public class GetPollDataEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Poll poll = Emulator.getGameEnvironment().getPollManager().getPoll(this.packet.readInt().intValue());
        if (poll != null) {
            this.client.sendResponse(new PollQuestionsComposer(poll));
        }
    }
}
