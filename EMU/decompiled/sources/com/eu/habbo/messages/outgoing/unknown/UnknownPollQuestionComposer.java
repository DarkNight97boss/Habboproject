package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownPollQuestionComposer.class */
public class UnknownPollQuestionComposer extends MessageComposer {
    private final int unknownInt;
    private final Map<String, Integer> unknownMap;

    public UnknownPollQuestionComposer(int i, Map<String, Integer> map) {
        this.unknownInt = i;
        this.unknownMap = map;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.SimplePollAnswersComposer);
        this.response.appendInt(Integer.valueOf(this.unknownInt));
        this.response.appendInt(Integer.valueOf(this.unknownMap.size()));
        for (Map.Entry<String, Integer> entry : this.unknownMap.entrySet()) {
            this.response.appendString(entry.getKey());
            this.response.appendInt(entry.getValue());
        }
        return this.response;
    }
}
