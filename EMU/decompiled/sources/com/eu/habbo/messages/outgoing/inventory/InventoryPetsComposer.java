package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.TIntObjectIterator;
import java.util.NoSuchElementException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/InventoryPetsComposer.class */
public class InventoryPetsComposer extends MessageComposer {
    private final Habbo habbo;

    public InventoryPetsComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.InventoryPetsComposer);
        this.response.appendInt((Integer) 1);
        this.response.appendInt((Integer) 1);
        this.response.appendInt(Integer.valueOf(this.habbo.getInventory().getPetsComponent().getPetsCount()));
        TIntObjectIterator it = this.habbo.getInventory().getPetsComponent().getPets().iterator();
        int size = this.habbo.getInventory().getPetsComponent().getPets().size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                ((Pet) it.value()).serialize(this.response);
            } catch (NoSuchElementException e) {
            }
        }
        return this.response;
    }
}
