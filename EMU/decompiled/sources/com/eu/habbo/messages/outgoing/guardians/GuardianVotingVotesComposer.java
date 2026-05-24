package com.eu.habbo.messages.outgoing.guardians;

import com.eu.habbo.habbohotel.guides.GuardianTicket;
import com.eu.habbo.habbohotel.guides.GuardianVote;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guardians/GuardianVotingVotesComposer.class */
public class GuardianVotingVotesComposer extends MessageComposer {
    private final GuardianTicket ticket;
    private final Habbo guardian;

    public GuardianVotingVotesComposer(GuardianTicket guardianTicket, Habbo habbo) {
        this.ticket = guardianTicket;
        this.guardian = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuardianVotingVotesComposer);
        ArrayList<GuardianVote> sortedVotes = this.ticket.getSortedVotes(this.guardian);
        this.response.appendInt(Integer.valueOf(sortedVotes.size()));
        Iterator<GuardianVote> it = sortedVotes.iterator();
        while (it.hasNext()) {
            this.response.appendInt(Integer.valueOf(it.next().type.getType()));
        }
        return this.response;
    }
}
