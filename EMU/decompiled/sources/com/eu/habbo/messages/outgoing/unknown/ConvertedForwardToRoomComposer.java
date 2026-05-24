package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/ConvertedForwardToRoomComposer.class */
public class ConvertedForwardToRoomComposer extends MessageComposer {
    private final String unknownString1;
    private final int unknownInt1;

    public ConvertedForwardToRoomComposer(String str, int i) {
        this.unknownString1 = str;
        this.unknownInt1 = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ConvertedForwardToRoomComposer);
        this.response.appendString(this.unknownString1);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        return this.response;
    }
}
