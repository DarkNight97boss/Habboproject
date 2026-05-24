package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/ClubGiftReceivedComposer.class */
public class ClubGiftReceivedComposer extends MessageComposer {
    private final String name;
    private final THashSet<Item> items;

    public ClubGiftReceivedComposer(String str, THashSet<Item> tHashSet) {
        this.name = str;
        this.items = tHashSet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ClubGiftReceivedComposer);
        this.response.appendString(this.name);
        this.response.appendInt(Integer.valueOf(this.items.size()));
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            ((Item) it.next()).serialize(this.response);
        }
        return this.response;
    }
}
