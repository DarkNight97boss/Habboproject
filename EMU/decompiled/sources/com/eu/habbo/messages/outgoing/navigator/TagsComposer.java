package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;
import java.util.Set;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/TagsComposer.class */
public class TagsComposer extends MessageComposer {
    private final Set<String> tags;

    public TagsComposer(Set<String> set) {
        this.tags = set;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TagsComposer);
        this.response.appendInt(Integer.valueOf(this.tags.size()));
        int i = 1;
        Iterator<String> it = this.tags.iterator();
        while (it.hasNext()) {
            this.response.appendString(it.next());
            this.response.appendInt(Integer.valueOf(i));
            i++;
        }
        return this.response;
    }
}
