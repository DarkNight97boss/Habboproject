package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/UnknownHabboWayQuizComposer.class */
public class UnknownHabboWayQuizComposer extends MessageComposer {
    private final String unknownString;
    private final List<Integer> unknownIntegerList;

    public UnknownHabboWayQuizComposer(String str, List<Integer> list) {
        this.unknownString = str;
        this.unknownIntegerList = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UnknownHabboWayQuizComposer);
        this.response.appendString(this.unknownString);
        this.response.appendInt(Integer.valueOf(this.unknownIntegerList.size()));
        Iterator<Integer> it = this.unknownIntegerList.iterator();
        while (it.hasNext()) {
            this.response.appendInt(it.next());
        }
        return this.response;
    }
}
