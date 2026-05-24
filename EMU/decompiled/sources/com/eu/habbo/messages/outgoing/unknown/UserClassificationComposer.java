package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.List;
import org.apache.commons.math3.util.Pair;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UserClassificationComposer.class */
public class UserClassificationComposer extends MessageComposer {
    private final List<Pair<Integer, Pair<String, String>>> info;

    public UserClassificationComposer(List<Pair<Integer, Pair<String, String>>> list) {
        this.info = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserClassificationComposer);
        this.response.appendInt(Integer.valueOf(this.info.size()));
        for (Pair<Integer, Pair<String, String>> pair : this.info) {
            this.response.appendInt((Integer) pair.getKey());
            this.response.appendString((String) ((Pair) pair.getValue()).getKey());
            this.response.appendString((String) ((Pair) pair.getValue()).getValue());
        }
        return this.response;
    }
}
