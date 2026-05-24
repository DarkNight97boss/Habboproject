package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/TalentTrackEmailVerifiedComposer.class */
public class TalentTrackEmailVerifiedComposer extends MessageComposer {
    private final String email;
    private final boolean unknownB1;
    private final boolean unknownB2;

    public TalentTrackEmailVerifiedComposer(String str, boolean z, boolean z2) {
        this.email = str;
        this.unknownB1 = z;
        this.unknownB2 = z2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TalentTrackEmailVerifiedComposer);
        this.response.appendString(this.email);
        this.response.appendBoolean(Boolean.valueOf(this.unknownB1));
        this.response.appendBoolean(Boolean.valueOf(this.unknownB2));
        return this.response;
    }
}
