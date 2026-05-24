package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.set.hash.THashSet;
import java.util.NoSuchElementException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/RoomFloorItemsComposer.class */
public class RoomFloorItemsComposer extends MessageComposer {
    private final TIntObjectMap<String> furniOwnerNames;
    private final THashSet<? extends HabboItem> items;

    public RoomFloorItemsComposer(TIntObjectMap<String> tIntObjectMap, THashSet<? extends HabboItem> tHashSet) {
        this.furniOwnerNames = tIntObjectMap;
        this.items = tHashSet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomFloorItemsComposer);
        TIntObjectIterator it = this.furniOwnerNames.iterator();
        this.response.appendInt(Integer.valueOf(this.furniOwnerNames.size()));
        int size = this.furniOwnerNames.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                this.response.appendInt(Integer.valueOf(it.key()));
                this.response.appendString((String) it.value());
            } catch (NoSuchElementException e) {
            }
        }
        this.response.appendInt(Integer.valueOf(this.items.size()));
        TObjectHashIterator it2 = this.items.iterator();
        while (it2.hasNext()) {
            HabboItem habboItem = (HabboItem) it2.next();
            habboItem.serializeFloorData(this.response);
            this.response.appendInt(Integer.valueOf(habboItem instanceof InteractionGift ? (((InteractionGift) habboItem).getColorId() * Outgoing.CraftableProductsComposer) + ((InteractionGift) habboItem).getRibbonId() : habboItem instanceof InteractionMusicDisc ? ((InteractionMusicDisc) habboItem).getSongId() : 1));
            habboItem.serializeExtradata(this.response);
            this.response.appendInt((Integer) (-1));
            this.response.appendInt(Integer.valueOf(habboItem.isUsable() ? 1 : 0));
            this.response.appendInt(Integer.valueOf(habboItem.getUserId()));
        }
        return this.response;
    }
}
