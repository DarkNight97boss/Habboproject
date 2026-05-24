package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/AddBotComposer.class */
public class AddBotComposer extends MessageComposer {
    private final Bot bot;

    public AddBotComposer(Bot bot) {
        this.bot = bot;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AddBotComposer);
        this.response.appendInt(Integer.valueOf(this.bot.getId()));
        this.response.appendString(this.bot.getName());
        this.response.appendString(this.bot.getMotto());
        this.response.appendString(this.bot.getGender().toString().toLowerCase().charAt(0) + Emulator.PREVIEW);
        this.response.appendString(this.bot.getFigure());
        this.response.appendBoolean(true);
        return this.response;
    }
}
