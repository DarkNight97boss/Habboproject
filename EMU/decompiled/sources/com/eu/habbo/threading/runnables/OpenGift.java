package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionGift;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryUpdateItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.PresentItemOpenedComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/OpenGift.class */
public class OpenGift implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGift.class);
    private final HabboItem item;
    private final Habbo habbo;
    private final Room room;

    public OpenGift(HabboItem habboItem, Habbo habbo, Room room) {
        this.item = habboItem;
        this.habbo = habbo;
        this.room = room;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            HabboItem habboItem = null;
            THashSet<HabboItem> tHashSetLoadItems = ((InteractionGift) this.item).loadItems();
            TObjectHashIterator it = tHashSetLoadItems.iterator();
            while (it.hasNext()) {
                HabboItem habboItem2 = (HabboItem) it.next();
                if (habboItem == null) {
                    habboItem = habboItem2;
                }
                habboItem2.setUserId(this.habbo.getHabboInfo().getId());
                habboItem2.needsUpdate(true);
                habboItem2.run();
            }
            if (habboItem != null) {
                habboItem.setFromGift(true);
            }
            this.habbo.getInventory().getItemsComponent().addItems(tHashSetLoadItems);
            RoomTile tile = this.room.getLayout().getTile(this.item.getX(), this.item.getY());
            if (tile != null) {
                this.room.updateTile(tile);
            }
            Emulator.getThreading().run(new QueryDeleteHabboItem(this.item.getId()));
            Emulator.getThreading().run(new RemoveFloorItemTask(this.room, this.item), this.item.getBaseItem().getName().contains("present_wrap") ? 5000L : 0L);
            this.habbo.getClient().sendResponse(new InventoryRefreshComposer());
            HashMap map = new HashMap();
            TObjectHashIterator it2 = tHashSetLoadItems.iterator();
            while (it2.hasNext()) {
                HabboItem habboItem3 = (HabboItem) it2.next();
                switch (habboItem3.getBaseItem().getType()) {
                    case WALL:
                    case FLOOR:
                        if (!map.containsKey(AddHabboItemComposer.AddHabboItemCategory.OWNED_FURNI)) {
                            map.put(AddHabboItemComposer.AddHabboItemCategory.OWNED_FURNI, new ArrayList());
                        }
                        ((List) map.get(AddHabboItemComposer.AddHabboItemCategory.OWNED_FURNI)).add(Integer.valueOf(habboItem3.getGiftAdjustedId()));
                        break;
                    case BADGE:
                        if (!map.containsKey(AddHabboItemComposer.AddHabboItemCategory.BADGE)) {
                            map.put(AddHabboItemComposer.AddHabboItemCategory.BADGE, new ArrayList());
                        }
                        ((List) map.get(AddHabboItemComposer.AddHabboItemCategory.BADGE)).add(Integer.valueOf(habboItem3.getId()));
                        break;
                    case PET:
                        if (!map.containsKey(AddHabboItemComposer.AddHabboItemCategory.PET)) {
                            map.put(AddHabboItemComposer.AddHabboItemCategory.PET, new ArrayList());
                        }
                        ((List) map.get(AddHabboItemComposer.AddHabboItemCategory.PET)).add(Integer.valueOf(habboItem3.getGiftAdjustedId()));
                        break;
                    case ROBOT:
                        if (!map.containsKey(AddHabboItemComposer.AddHabboItemCategory.BOT)) {
                            map.put(AddHabboItemComposer.AddHabboItemCategory.BOT, new ArrayList());
                        }
                        ((List) map.get(AddHabboItemComposer.AddHabboItemCategory.BOT)).add(Integer.valueOf(habboItem3.getGiftAdjustedId()));
                        break;
                }
            }
            this.habbo.getClient().sendResponse(new AddHabboItemComposer(map));
            if (habboItem != null) {
                this.habbo.getClient().sendResponse(new InventoryUpdateItemComposer(habboItem));
                this.habbo.getClient().sendResponse(new PresentItemOpenedComposer(habboItem, Emulator.PREVIEW, false));
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }
}
