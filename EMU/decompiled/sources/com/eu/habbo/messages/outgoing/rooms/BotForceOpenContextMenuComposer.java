package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/BotForceOpenContextMenuComposer.class */
public class BotForceOpenContextMenuComposer extends MessageComposer {
    private final int botId;

    public BotForceOpenContextMenuComposer(int i) {
        this.botId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BotForceOpenContextMenuComposer);
        this.response.appendInt(Integer.valueOf(this.botId));
        return this.response;
    }
}
