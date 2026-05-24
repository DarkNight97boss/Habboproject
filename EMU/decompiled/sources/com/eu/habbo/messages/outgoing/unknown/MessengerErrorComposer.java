package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/MessengerErrorComposer.class */
public class MessengerErrorComposer extends MessageComposer {
    private final Map<Integer, Integer> errors;

    public MessengerErrorComposer(Map<Integer, Integer> map) {
        this.errors = map;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MessengerErrorComposer);
        this.response.appendInt(Integer.valueOf(this.errors.size()));
        for (Map.Entry<Integer, Integer> entry : this.errors.entrySet()) {
            this.response.appendInt(entry.getKey());
            this.response.appendInt(entry.getValue());
        }
        return this.response;
    }
}
