package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/RoomAdErrorComposer.class */
public class RoomAdErrorComposer extends MessageComposer {
    private final int errorCode;
    private final String unknownString;

    public RoomAdErrorComposer(int i, String str) {
        this.errorCode = i;
        this.unknownString = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomAdErrorComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        this.response.appendString(this.unknownString);
        return this.response;
    }
}
