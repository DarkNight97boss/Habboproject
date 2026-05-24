package com.eu.habbo.messages.incoming.crafting;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.crafting.CraftingRecipe;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertLimitedSoldOutComposer;
import com.eu.habbo.messages.outgoing.crafting.CraftingRecipeComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/crafting/CraftingAddRecipeEvent.class */
public class CraftingAddRecipeEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        CraftingRecipe recipe = Emulator.getGameEnvironment().getCraftingManager().getRecipe(this.packet.readString());
        if (recipe != null) {
            if (recipe.canBeCrafted()) {
                this.client.sendResponse(new CraftingRecipeComposer(recipe));
            } else {
                this.client.sendResponse(new AlertLimitedSoldOutComposer());
            }
        }
    }
}
