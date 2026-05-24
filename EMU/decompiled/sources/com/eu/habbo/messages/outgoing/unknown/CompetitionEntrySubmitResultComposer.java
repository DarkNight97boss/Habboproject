package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/CompetitionEntrySubmitResultComposer.class */
public class CompetitionEntrySubmitResultComposer extends MessageComposer {
    private final int unknownInt1;
    private final String unknownString1;
    private final int result;
    private final List<String> unknownStringList1;
    private final List<String> unknownStringList2;

    public CompetitionEntrySubmitResultComposer(int i, String str, int i2, List<String> list, List<String> list2) {
        this.unknownInt1 = i;
        this.unknownString1 = str;
        this.result = i2;
        this.unknownStringList1 = list;
        this.unknownStringList2 = list2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CompetitionEntrySubmitResultComposer);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        this.response.appendString(this.unknownString1);
        this.response.appendInt(Integer.valueOf(this.result));
        this.response.appendInt(Integer.valueOf(this.unknownStringList1.size()));
        Iterator<String> it = this.unknownStringList1.iterator();
        while (it.hasNext()) {
            this.response.appendString(it.next());
        }
        this.response.appendInt(Integer.valueOf(this.unknownStringList2.size()));
        Iterator<String> it2 = this.unknownStringList2.iterator();
        while (it2.hasNext()) {
            this.response.appendString(it2.next());
        }
        return this.response;
    }
}
