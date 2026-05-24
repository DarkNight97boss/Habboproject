package com.eu.habbo.messages.outgoing.events.mysticbox;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/events/mysticbox/MysticBoxPrizeComposer.class */
public class MysticBoxPrizeComposer extends MessageComposer {
    private final String type;
    private final int itemId;

    public MysticBoxPrizeComposer(String str, int i) {
        this.type = str;
        this.itemId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MysticBoxPrizeComposer);
        this.response.appendString(this.type);
        this.response.appendInt(Integer.valueOf(this.itemId));
        return this.response;
    }
}
