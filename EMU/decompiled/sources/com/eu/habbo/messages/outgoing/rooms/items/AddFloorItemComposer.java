package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/AddFloorItemComposer.class */
public class AddFloorItemComposer extends MessageComposer {
    private final HabboItem item;
    private final String itemOwnerName;

    public AddFloorItemComposer(HabboItem habboItem, String str) {
        this.item = habboItem;
        this.itemOwnerName = str == null ? Emulator.PREVIEW : str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AddFloorItemComposer);
        this.item.serializeFloorData(this.response);
        this.response.appendInt(Integer.valueOf(this.item instanceof InteractionGift ? (((InteractionGift) this.item).getColorId() * Outgoing.CraftableProductsComposer) + ((InteractionGift) this.item).getRibbonId() : this.item instanceof InteractionMusicDisc ? ((InteractionMusicDisc) this.item).getSongId() : 1));
        this.item.serializeExtradata(this.response);
        this.response.appendInt((Integer) (-1));
        this.response.appendInt(Boolean.valueOf(this.item.isUsable()));
        this.response.appendInt(Integer.valueOf(this.item.getUserId()));
        this.response.appendString(this.itemOwnerName);
        return this.response;
    }
}
