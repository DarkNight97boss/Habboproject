package com.eu.habbo.messages.incoming.crafting;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.crafting.CraftingAltar;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.crafting.CraftableProductsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/crafting/RequestCraftingRecipesEvent.class */
public class RequestCraftingRecipesEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        CraftingAltar altar;
        HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(this.packet.readInt().intValue());
        if (habboItem == null || (altar = Emulator.getGameEnvironment().getCraftingManager().getAltar(habboItem.getBaseItem())) == null) {
            return;
        }
        this.client.sendResponse(new CraftableProductsComposer(altar.getRecipesForHabbo(this.client.getHabbo()), altar.getIngredients()));
    }
}
