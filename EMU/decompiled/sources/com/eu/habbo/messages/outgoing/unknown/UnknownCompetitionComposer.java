package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownCompetitionComposer.class */
public class UnknownCompetitionComposer extends MessageComposer {
    private final int unknownInt1;
    private final String unknownString1;
    private final int unknownInt2;
    private final int unknownInt3;

    public UnknownCompetitionComposer(int i, String str, int i2, int i3) {
        this.unknownInt1 = i;
        this.unknownString1 = str;
        this.unknownInt2 = i2;
        this.unknownInt3 = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownCompetitionComposer);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        this.response.appendString(this.unknownString1);
        this.response.appendInt(Integer.valueOf(this.unknownInt2));
        this.response.appendInt(Integer.valueOf(this.unknownInt3));
        return this.response;
    }
}
