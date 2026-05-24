package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/GiftConfigurationComposer.class */
public class GiftConfigurationComposer extends MessageComposer {
    public static List<Integer> BOX_TYPES = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 8);
    public static List<Integer> RIBBON_TYPES = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GiftConfigurationComposer);
        this.response.appendBoolean(true);
        this.response.appendInt(Integer.valueOf(Emulator.getConfig().getInt("hotel.gifts.special.price", 2)));
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getCatalogManager().giftWrappers.size()));
        Iterator it = Emulator.getGameEnvironment().getCatalogManager().giftWrappers.keySet().iterator();
        while (it.hasNext()) {
            this.response.appendInt((Integer) it.next());
        }
        this.response.appendInt(Integer.valueOf(BOX_TYPES.size()));
        Iterator<Integer> it2 = BOX_TYPES.iterator();
        while (it2.hasNext()) {
            this.response.appendInt(it2.next());
        }
        this.response.appendInt(Integer.valueOf(RIBBON_TYPES.size()));
        Iterator<Integer> it3 = RIBBON_TYPES.iterator();
        while (it3.hasNext()) {
            this.response.appendInt(it3.next());
        }
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getCatalogManager().giftFurnis.size()));
        Iterator it4 = Emulator.getGameEnvironment().getCatalogManager().giftFurnis.entrySet().iterator();
        while (it4.hasNext()) {
            this.response.appendInt((Integer) ((Map.Entry) it4.next()).getKey());
        }
        return this.response;
    }
}
