package com.eu.habbo.messages.incoming.crafting;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.crafting.CraftingRecipe;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertLimitedSoldOutComposer;
import com.eu.habbo.messages.outgoing.crafting.CraftingResultComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItems;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/crafting/CraftingCraftItemEvent.class */
public class CraftingCraftItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        CraftingRecipe recipe = Emulator.getGameEnvironment().getCraftingManager().getAltar(this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(this.packet.readInt().intValue()).getBaseItem()).getRecipe(this.packet.readString());
        if (recipe != null) {
            if (!recipe.canBeCrafted()) {
                this.client.sendResponse(new AlertLimitedSoldOutComposer());
                return;
            }
            TIntObjectHashMap tIntObjectHashMap = new TIntObjectHashMap();
            for (Map.Entry entry : recipe.getIngredients().entrySet()) {
                for (int i = 0; i < ((Integer) entry.getValue()).intValue(); i++) {
                    HabboItem andRemoveHabboItem = this.client.getHabbo().getInventory().getItemsComponent().getAndRemoveHabboItem((Item) entry.getKey());
                    if (andRemoveHabboItem == null) {
                        return;
                    }
                    tIntObjectHashMap.put(andRemoveHabboItem.getId(), andRemoveHabboItem);
                }
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
                this.client.getHabbo().getInventory().getItemsComponent().addItem(habboItemCreateItem);
                this.client.sendResponse(new AddHabboItemComposer(habboItemCreateItem));
                AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("Atcg"));
                tIntObjectHashMap.forEachValue(habboItem -> {
                    this.client.sendResponse(new RemoveHabboItemComposer(habboItem.getGiftAdjustedId()));
                    return true;
                });
                this.client.sendResponse(new InventoryRefreshComposer());
                Emulator.getThreading().run(new QueryDeleteHabboItems(tIntObjectHashMap));
                return;
            }
        }
        this.client.sendResponse(new CraftingResultComposer(null));
    }
}
