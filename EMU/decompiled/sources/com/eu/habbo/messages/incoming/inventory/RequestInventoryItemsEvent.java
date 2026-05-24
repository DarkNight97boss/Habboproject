package com.eu.habbo.messages.incoming.inventory;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.InventoryItemsComposer;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/inventory/RequestInventoryItemsEvent.class */
public class RequestInventoryItemsEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestInventoryItemsEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int size = this.client.getHabbo().getInventory().getItemsComponent().getItems().size();
        if (size == 0) {
            this.client.sendResponse(new InventoryItemsComposer(0, 1, new TIntObjectHashMap()));
            return;
        }
        int iCeil = (int) Math.ceil(((double) size) / 1000.0d);
        if (iCeil == 0) {
            iCeil = 1;
        }
        synchronized (this.client.getHabbo().getInventory().getItemsComponent().getItems()) {
            TIntObjectHashMap tIntObjectHashMap = new TIntObjectHashMap();
            TIntObjectIterator it = this.client.getHabbo().getInventory().getItemsComponent().getItems().iterator();
            int i = 0;
            int i2 = 0;
            int size2 = this.client.getHabbo().getInventory().getItemsComponent().getItems().size();
            while (true) {
                int i3 = size2;
                size2--;
                if (i3 <= 0) {
                    break;
                }
                if (i == 0) {
                    i2++;
                }
                try {
                    it.advance();
                    tIntObjectHashMap.put(it.key(), (HabboItem) it.value());
                    i++;
                    if (i == 1000) {
                        this.client.sendResponse(new InventoryItemsComposer(i2, iCeil, tIntObjectHashMap));
                        i = 0;
                        tIntObjectHashMap.clear();
                    }
                } catch (NoSuchElementException e) {
                    LOGGER.error("Caught exception", e);
                    if (i > 0) {
                        this.client.sendResponse(new InventoryItemsComposer(i2, iCeil, tIntObjectHashMap));
                    }
                }
            }
            if (i > 0 && tIntObjectHashMap.size() > 0) {
                this.client.sendResponse(new InventoryItemsComposer(i2, iCeil, tIntObjectHashMap));
            }
        }
    }
}
