package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/RoomUserQuestionAnsweredComposer.class */
public class RoomUserQuestionAnsweredComposer extends MessageComposer {
    private final int userId;
    private final String value;
    private final Map<String, Integer> unknownMap;

    public RoomUserQuestionAnsweredComposer(int i, String str, Map<String, Integer> map) {
        this.userId = i;
        this.value = str;
        this.unknownMap = map;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(-1);
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendString(this.value);
        this.response.appendInt(Integer.valueOf(this.unknownMap.size()));
        for (Map.Entry<String, Integer> entry : this.unknownMap.entrySet()) {
            this.response.appendString(entry.getKey());
            this.response.appendInt(entry.getValue());
        }
        return this.response;
    }
}
