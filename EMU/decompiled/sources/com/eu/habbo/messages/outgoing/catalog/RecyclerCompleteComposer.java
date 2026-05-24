package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/RecyclerCompleteComposer.class */
public class RecyclerCompleteComposer extends MessageComposer {
    public static final int RECYCLING_COMPLETE = 1;
    public static final int RECYCLING_CLOSED = 2;
    private final int code;

    public RecyclerCompleteComposer(int i) {
        this.code = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RecyclerCompleteComposer);
        this.response.appendInt(Integer.valueOf(this.code));
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
