package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/MessagesForYouComposer.class */
public class MessagesForYouComposer extends MessageComposer {
    private final String[] messages;
    private final List<String> newMessages;

    public MessagesForYouComposer(String[] strArr) {
        this.messages = strArr;
        this.newMessages = new ArrayList();
    }

    public MessagesForYouComposer(List<String> list) {
        this.newMessages = list;
        this.messages = new String[0];
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MessagesForYouComposer);
        this.response.appendInt(Integer.valueOf(this.messages.length + this.newMessages.size()));
        for (String str : this.messages) {
            this.response.appendString(str);
        }
        Iterator<String> it = this.newMessages.iterator();
        while (it.hasNext()) {
            this.response.appendString(it.next());
        }
        return this.response;
    }
}
