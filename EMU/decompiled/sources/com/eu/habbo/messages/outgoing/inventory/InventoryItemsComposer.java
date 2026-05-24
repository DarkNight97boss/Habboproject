package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntObjectProcedure;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/InventoryItemsComposer.class */
public class InventoryItemsComposer extends MessageComposer implements TIntObjectProcedure<HabboItem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryItemsComposer.class);
    private final int fragmentNumber;
    private final int totalFragments;
    private final TIntObjectMap<HabboItem> items;

    public InventoryItemsComposer(int i, int i2, TIntObjectMap<HabboItem> tIntObjectMap) {
        this.fragmentNumber = i;
        this.totalFragments = i2;
        this.items = tIntObjectMap;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        try {
            this.response.init(Outgoing.InventoryItemsComposer);
            this.response.appendInt(Integer.valueOf(this.totalFragments));
            this.response.appendInt(Integer.valueOf(this.fragmentNumber - 1));
            this.response.appendInt(Integer.valueOf(this.items.size()));
            this.items.forEachEntry(this);
            return this.response;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return null;
        }
    }

    public boolean execute(int i, HabboItem habboItem) {
        this.response.appendInt(Integer.valueOf(habboItem.getGiftAdjustedId()));
        this.response.appendString(habboItem.getBaseItem().getType().code);
        this.response.appendInt(Integer.valueOf(habboItem.getId()));
        this.response.appendInt(Integer.valueOf(habboItem.getBaseItem().getSpriteId()));
        if (habboItem.getBaseItem().getName().equals("floor") || habboItem.getBaseItem().getName().equals("landscape") || habboItem.getBaseItem().getName().equals("song_disk") || habboItem.getBaseItem().getName().equals("wallpaper") || habboItem.getBaseItem().getName().equals("poster")) {
            switch (habboItem.getBaseItem().getName()) {
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
                case "song_disk":
                    this.response.appendInt((Integer) 8);
                    break;
            }
            addExtraDataToResponse(habboItem);
        } else {
            if (habboItem.getBaseItem().getName().equals("gnome_box")) {
                this.response.appendInt((Integer) 13);
            } else {
                this.response.appendInt(Integer.valueOf(habboItem instanceof InteractionGift ? (((InteractionGift) habboItem).getColorId() * Outgoing.CraftableProductsComposer) + ((InteractionGift) habboItem).getRibbonId() : 1));
            }
            habboItem.serializeExtradata(this.response);
        }
        this.response.appendBoolean(Boolean.valueOf(habboItem.getBaseItem().allowRecyle()));
        this.response.appendBoolean(Boolean.valueOf(habboItem.getBaseItem().allowTrade()));
        this.response.appendBoolean(Boolean.valueOf(!habboItem.isLimited() && habboItem.getBaseItem().allowInventoryStack()));
        this.response.appendBoolean(Boolean.valueOf(habboItem.getBaseItem().allowMarketplace()));
        this.response.appendInt((Integer) (-1));
        this.response.appendBoolean(true);
        this.response.appendInt((Integer) (-1));
        if (habboItem.getBaseItem().getType() != FurnitureType.FLOOR) {
            return true;
        }
        this.response.appendString(Emulator.PREVIEW);
        if (!habboItem.getBaseItem().getName().equals("song_disk")) {
            this.response.appendInt(Integer.valueOf(habboItem instanceof InteractionGift ? (((InteractionGift) habboItem).getColorId() * Outgoing.CraftableProductsComposer) + ((InteractionGift) habboItem).getRibbonId() : 1));
            return true;
        }
        List listAsList = Arrays.asList(habboItem.getExtradata().split("\n"));
        this.response.appendInt(Integer.valueOf((String) listAsList.get(listAsList.size() - 1)));
        return true;
    }

    public void addExtraDataToResponse(HabboItem habboItem) {
        this.response.appendInt((Integer) 0);
        this.response.appendString(habboItem.getExtradata());
    }
}
