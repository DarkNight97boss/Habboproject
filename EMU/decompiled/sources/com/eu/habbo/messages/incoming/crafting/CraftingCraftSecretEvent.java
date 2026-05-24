package com.eu.habbo.messages.incoming.crafting;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.crafting.CraftingAltar;
import com.eu.habbo.habbohotel.crafting.CraftingRecipe;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertLimitedSoldOutComposer;
import com.eu.habbo.messages.outgoing.crafting.CraftingResultComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/crafting/CraftingCraftSecretEvent.class */
public class CraftingCraftSecretEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        CraftingAltar altar;
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue);
        if (habboItem != null && (altar = Emulator.getGameEnvironment().getCraftingManager().getAltar(habboItem.getBaseItem())) != null) {
            THashSet<HabboItem> tHashSet = new THashSet();
            THashMap tHashMap = new THashMap();
            for (int i = 0; i < iIntValue2; i++) {
                HabboItem habboItem2 = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(this.packet.readInt().intValue());
                if (habboItem2 == null) {
                    this.client.sendResponse(new CraftingResultComposer(null));
                    return;
                }
                tHashSet.add(habboItem2);
                if (!tHashMap.containsKey(habboItem2.getBaseItem())) {
                    tHashMap.put(habboItem2.getBaseItem(), 0);
                }
                tHashMap.put(habboItem2.getBaseItem(), Integer.valueOf(tHashMap.get(habboItem2.getBaseItem()).intValue() + 1));
            }
            CraftingRecipe recipe = altar.getRecipe((Map<Item, Integer>) tHashMap);
            if (recipe != null) {
                if (!recipe.canBeCrafted()) {
                    this.client.sendResponse(new AlertLimitedSoldOutComposer());
                    return;
                }
                HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(this.client.getHabbo().getHabboInfo().getId(), recipe.getReward(), 0, 0, Emulator.PREVIEW);
                if (habboItemCreateItem != null) {
                    if (recipe.isLimited()) {
                        recipe.decrease();
                    }
                    if (!recipe.getAchievement().isEmpty()) {
                        AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement(recipe.getAchievement()));
                    }
                    this.client.sendResponse(new CraftingResultComposer(recipe));
                    if (!this.client.getHabbo().getHabboStats().hasRecipe(recipe.getId())) {
                        this.client.getHabbo().getHabboStats().addRecipe(recipe.getId());
                        AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("AtcgSecret"));
                    }
                    this.client.getHabbo().getInventory().getItemsComponent().addItem(habboItemCreateItem);
                    this.client.sendResponse(new AddHabboItemComposer(habboItemCreateItem));
                    for (HabboItem habboItem3 : tHashSet) {
                        this.client.getHabbo().getInventory().getItemsComponent().removeHabboItem(habboItem3);
                        this.client.sendResponse(new RemoveHabboItemComposer(habboItem3.getGiftAdjustedId()));
                        Emulator.getThreading().run(new QueryDeleteHabboItem(habboItem3.getId()));
                    }
                    this.client.sendResponse(new InventoryRefreshComposer());
                    return;
                }
            }
        }
        this.client.sendResponse(new CraftingResultComposer(null));
    }
}
