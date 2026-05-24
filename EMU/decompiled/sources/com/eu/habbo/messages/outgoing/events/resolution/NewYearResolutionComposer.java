package com.eu.habbo.messages.outgoing.events.resolution;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/events/resolution/NewYearResolutionComposer.class */
public class NewYearResolutionComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(66);
        this.response.appendInt((Integer) 230);
        this.response.appendInt((Integer) 2);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        this.response.appendString("NY2013RES");
        this.response.appendInt((Integer) 3);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 2);
        this.response.appendInt((Integer) 1);
        this.response.appendString("ADM");
        this.response.appendInt((Integer) 2);
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(Outgoing.CraftableProductsComposer));
        return this.response;
    }
}
