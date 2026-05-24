package com.eu.habbo.messages.outgoing.guardians;

import com.eu.habbo.habbohotel.guides.GuardianTicket;
import com.eu.habbo.habbohotel.guides.GuardianVote;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guardians/GuardianVotingResultComposer.class */
public class GuardianVotingResultComposer extends MessageComposer {
    private final GuardianTicket ticket;
    private final GuardianVote vote;

    public GuardianVotingResultComposer(GuardianTicket guardianTicket, GuardianVote guardianVote) {
        this.ticket = guardianTicket;
        this.vote = guardianVote;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuardianVotingResultComposer);
        this.response.appendInt(Integer.valueOf(this.ticket.getVerdict().getType()));
        this.response.appendInt(Integer.valueOf(this.vote.type.getType()));
        this.response.appendInt(Integer.valueOf(this.ticket.getVotes().size() - 1));
        for (Map.Entry entry : this.ticket.getVotes().entrySet()) {
            if (!((GuardianVote) entry.getValue()).equals(this.vote)) {
                this.response.appendInt(Integer.valueOf(((GuardianVote) entry.getValue()).type.getType()));
            }
        }
        return this.response;
    }
}
