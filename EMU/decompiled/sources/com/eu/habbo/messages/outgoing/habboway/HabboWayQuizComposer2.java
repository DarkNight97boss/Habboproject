package com.eu.habbo.messages.outgoing.habboway;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/habboway/HabboWayQuizComposer2.class */
public class HabboWayQuizComposer2 extends MessageComposer {
    public final String name;
    public final int[] items;

    public HabboWayQuizComposer2(String str, int[] iArr) {
        this.name = str;
        this.items = iArr;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.HabboWayQuizComposer2);
        this.response.appendString(this.name);
        this.response.appendInt(Integer.valueOf(this.items.length));
        for (int i : this.items) {
            this.response.appendInt(Integer.valueOf(i));
        }
        return this.response;
    }
}
