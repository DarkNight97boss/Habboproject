package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/InventoryUpdateItemComposer.class */
public class InventoryUpdateItemComposer extends MessageComposer {
    private final HabboItem habboItem;

    public InventoryUpdateItemComposer(HabboItem habboItem) {
        this.habboItem = habboItem;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.InventoryItemUpdateComposer);
        this.response.appendInt(Integer.valueOf(this.habboItem.getGiftAdjustedId()));
        this.response.appendString(this.habboItem.getBaseItem().getType().code);
        this.response.appendInt(Integer.valueOf(this.habboItem.getId()));
        this.response.appendInt(Integer.valueOf(this.habboItem.getBaseItem().getSpriteId()));
        switch (this.habboItem.getBaseItem().getName()) {
            case "landscape":
                this.response.appendInt((Integer) 4);
                break;
            case "floor":
                this.response.appendInt((Integer) 3);
                break;
            case "wallpaper":
                this.response.appendInt((Integer) 2);
                break;
            case "poster":
                this.response.appendInt((Integer) 6);
                break;
        }
        if (this.habboItem.isLimited()) {
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 256);
            this.response.appendString(this.habboItem.getExtradata());
            this.response.appendInt(Integer.valueOf(this.habboItem.getLimitedSells()));
            this.response.appendInt(Integer.valueOf(this.habboItem.getLimitedStack()));
        } else {
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 0);
            this.response.appendString(this.habboItem.getExtradata());
        }
        this.response.appendBoolean(Boolean.valueOf(this.habboItem.getBaseItem().allowRecyle()));
        this.response.appendBoolean(Boolean.valueOf(this.habboItem.getBaseItem().allowTrade()));
        this.response.appendBoolean(Boolean.valueOf(!this.habboItem.isLimited() && this.habboItem.getBaseItem().allowInventoryStack()));
        this.response.appendBoolean(Boolean.valueOf(this.habboItem.getBaseItem().allowMarketplace()));
        this.response.appendInt((Integer) (-1));
        this.response.appendBoolean(false);
        this.response.appendInt((Integer) (-1));
        if (this.habboItem.getBaseItem().getType() == FurnitureType.FLOOR) {
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt((Integer) 0);
        }
        this.response.appendInt((Integer) 100);
        return this.response;
    }
}
