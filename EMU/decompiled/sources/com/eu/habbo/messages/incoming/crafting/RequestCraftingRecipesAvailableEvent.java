package com.eu.habbo.messages.incoming.crafting;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.crafting.CraftingAltar;
import com.eu.habbo.habbohotel.crafting.CraftingRecipe;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.crafting.CraftingRecipesAvailableComposer;
import gnu.trove.map.hash.THashMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/crafting/RequestCraftingRecipesAvailableEvent.class */
public class RequestCraftingRecipesAvailableEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        CraftingAltar altar = Emulator.getGameEnvironment().getCraftingManager().getAltar(this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(this.packet.readInt().intValue()).getBaseItem());
        if (altar != null) {
            THashMap tHashMap = new THashMap();
            int iIntValue = this.packet.readInt().intValue();
            for (int i = 0; i < iIntValue; i++) {
                HabboItem habboItem = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(this.packet.readInt().intValue());
                if (habboItem != null) {
                    if (!tHashMap.containsKey(habboItem.getBaseItem())) {
                        tHashMap.put(habboItem.getBaseItem(), 0);
                    }
                    tHashMap.put(habboItem.getBaseItem(), Integer.valueOf(tHashMap.get(habboItem.getBaseItem()).intValue() + 1));
                }
            }
            CraftingRecipe recipe = altar.getRecipe((Map<Item, Integer>) tHashMap);
            if (recipe == null || !this.client.getHabbo().getHabboStats().hasRecipe(recipe.getId())) {
                Map<CraftingRecipe, Boolean> mapMatchRecipes = altar.matchRecipes(tHashMap);
                boolean z = false;
                int size = mapMatchRecipes.size();
                Iterator<Map.Entry<CraftingRecipe, Boolean>> it = mapMatchRecipes.entrySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Map.Entry<CraftingRecipe, Boolean> next = it.next();
                    if (!this.client.getHabbo().getHabboStats().hasRecipe(next.getKey().getId())) {
                        if (next.getValue().booleanValue()) {
                            z = true;
                            break;
                        }
                    } else {
                        size--;
                    }
                }
                this.client.sendResponse(new CraftingRecipesAvailableComposer(size, z));
            }
        }
    }
}
