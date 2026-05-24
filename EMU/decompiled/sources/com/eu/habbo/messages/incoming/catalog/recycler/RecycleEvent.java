package com.eu.habbo.messages.incoming.catalog.recycler;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.ItemManager;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.RecyclerCompleteComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.HotelWillCloseInMinutesComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import com.eu.habbo.threading.runnables.ShutdownEmulator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/recycler/RecycleEvent.class */
public class RecycleEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (ShutdownEmulator.timestamp > 0) {
            this.client.sendResponse(new HotelWillCloseInMinutesComposer((ShutdownEmulator.timestamp - Emulator.getIntUnixTimestamp()) / 60));
            return;
        }
        if (Emulator.getGameEnvironment().getCatalogManager().ecotronItem == null || !ItemManager.RECYCLER_ENABLED) {
            this.client.sendResponse(new RecyclerCompleteComposer(2));
            return;
        }
        THashSet tHashSet = new THashSet();
        int iIntValue = this.packet.readInt().intValue();
        if (iIntValue < Emulator.getConfig().getInt("recycler.value", 8)) {
            return;
        }
        for (int i = 0; i < iIntValue; i++) {
            HabboItem habboItem = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(this.packet.readInt().intValue());
            if (habboItem == null) {
                return;
            }
            if (habboItem.getBaseItem().allowRecyle()) {
                tHashSet.add(habboItem);
            }
        }
        if (tHashSet.size() != iIntValue) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            return;
        }
        TObjectHashIterator it = tHashSet.iterator();
        while (it.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it.next();
            this.client.getHabbo().getInventory().getItemsComponent().removeHabboItem(habboItem2);
            this.client.sendResponse(new RemoveHabboItemComposer(habboItem2.getGiftAdjustedId()));
            Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem2.getId()));
        }
        HabboItem habboItemHandleRecycle = Emulator.getGameEnvironment().getItemManager().handleRecycle(this.client.getHabbo(), Emulator.getGameEnvironment().getCatalogManager().getRandomRecyclerPrize().getId() + Emulator.PREVIEW);
        if (habboItemHandleRecycle == null) {
            this.client.sendResponse(new AlertPurchaseFailedComposer(0));
            return;
        }
        this.client.sendResponse(new AddHabboItemComposer(habboItemHandleRecycle));
        this.client.getHabbo().getInventory().getItemsComponent().addItem(habboItemHandleRecycle);
        this.client.sendResponse(new RecyclerCompleteComposer(1));
        this.client.sendResponse(new InventoryRefreshComposer());
        AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FurnimaticQuest"));
    }
}
