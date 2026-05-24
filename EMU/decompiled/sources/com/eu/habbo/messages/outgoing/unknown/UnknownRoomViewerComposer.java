package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownRoomViewerComposer.class */
public class UnknownRoomViewerComposer extends MessageComposer {
    private final Map<Integer, String> unknownMap;

    public UnknownRoomViewerComposer(Map<Integer, String> map) {
        this.unknownMap = map;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownRoomViewerComposer);
        this.response.appendInt(Integer.valueOf(this.unknownMap.size()));
        for (Map.Entry<Integer, String> entry : this.unknownMap.entrySet()) {
            this.response.appendInt(entry.getKey());
            this.response.appendString(entry.getValue());
        }
        return this.response;
    }
}
