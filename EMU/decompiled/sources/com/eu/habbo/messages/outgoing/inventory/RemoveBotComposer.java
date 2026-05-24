package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/RemoveBotComposer.class */
public class RemoveBotComposer extends MessageComposer {
    private final Bot bot;

    public RemoveBotComposer(Bot bot) {
        this.bot = bot;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RemoveBotComposer);
        this.response.appendInt(Integer.valueOf(this.bot.getId()));
        return this.response;
    }
}
