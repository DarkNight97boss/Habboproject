package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/RecyclerLogicComposer.class */
public class RecyclerLogicComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RecyclerLogicComposer);
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getCatalogManager().prizes.size()));
        for (Map.Entry entry : Emulator.getGameEnvironment().getCatalogManager().prizes.entrySet()) {
            this.response.appendInt((Integer) entry.getKey());
            this.response.appendInt(Integer.valueOf(Emulator.getConfig().getValue("hotel.ecotron.rarity.chance." + entry.getKey())));
            this.response.appendInt(Integer.valueOf(((THashSet) entry.getValue()).size()));
            TObjectHashIterator it = ((THashSet) entry.getValue()).iterator();
            while (it.hasNext()) {
                Item item = (Item) it.next();
                this.response.appendString(item.getName());
                this.response.appendInt((Integer) 1);
                this.response.appendString(item.getType().code.toLowerCase());
                this.response.appendInt(Integer.valueOf(item.getSpriteId()));
            }
        }
        return this.response;
    }
}
