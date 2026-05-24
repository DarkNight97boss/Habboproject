package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownRoomDesktopComposer.class */
public class UnknownRoomDesktopComposer extends MessageComposer {
    private final int unknownInt1;
    private final Map<Integer, String> unknownMap;

    public UnknownRoomDesktopComposer(int i, Map<Integer, String> map) {
        this.unknownInt1 = i;
        this.unknownMap = map;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(69);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        this.response.appendInt(Integer.valueOf(this.unknownMap.size()));
        for (Map.Entry<Integer, String> entry : this.unknownMap.entrySet()) {
            this.response.appendInt(entry.getKey());
            this.response.appendString(entry.getValue());
        }
        return this.response;
    }
}
