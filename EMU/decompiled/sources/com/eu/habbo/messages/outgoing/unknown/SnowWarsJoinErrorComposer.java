package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/SnowWarsJoinErrorComposer.class */
public class SnowWarsJoinErrorComposer extends MessageComposer {
    public static final int ERROR_HAS_ACTIVE_INSTANCE = 6;
    public static final int ERROR_NO_FREE_GAMES_LEFT = 8;
    public static final int ERROR_DUPLICATE_MACHINE_ID = 2;

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownComposer_1188);
        this.response.appendInt((Integer) 2);
        return this.response;
    }
}
