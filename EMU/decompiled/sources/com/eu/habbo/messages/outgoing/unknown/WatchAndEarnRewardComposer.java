package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/WatchAndEarnRewardComposer.class */
public class WatchAndEarnRewardComposer extends MessageComposer {
    private final Item item;

    public WatchAndEarnRewardComposer(Item item) {
        this.item = item;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.WatchAndEarnRewardComposer);
        this.response.appendString(this.item.getType().code);
        this.response.appendInt(Integer.valueOf(this.item.getId()));
        this.response.appendString(this.item.getName());
        this.response.appendString(this.item.getFullName());
        return this.response;
    }
}
