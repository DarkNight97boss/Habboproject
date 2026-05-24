package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/FloorItemUpdateComposer.class */
public class FloorItemUpdateComposer extends MessageComposer {
    private final HabboItem item;

    public FloorItemUpdateComposer(HabboItem habboItem) {
        this.item = habboItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FloorItemUpdateComposer);
        this.item.serializeFloorData(this.response);
        this.response.appendInt(Integer.valueOf(this.item instanceof InteractionGift ? (((InteractionGift) this.item).getColorId() * Outgoing.CraftableProductsComposer) + ((InteractionGift) this.item).getRibbonId() : this.item instanceof InteractionMusicDisc ? ((InteractionMusicDisc) this.item).getSongId() : this.item.isUsable() ? 0 : 0));
        this.item.serializeExtradata(this.response);
        this.response.appendInt((Integer) (-1));
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(this.item.getUserId()));
        return this.response;
    }
}
