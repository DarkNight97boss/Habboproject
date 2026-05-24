package com.eu.habbo.messages.outgoing.guardians;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guardians/GuardianVotingTimeEnded.class */
public class GuardianVotingTimeEnded extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(30);
        return this.response;
    }
}
