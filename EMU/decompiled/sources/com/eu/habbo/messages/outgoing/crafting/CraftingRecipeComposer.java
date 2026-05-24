package com.eu.habbo.messages.outgoing.crafting;

import com.eu.habbo.habbohotel.crafting.CraftingRecipe;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/crafting/CraftingRecipeComposer.class */
public class CraftingRecipeComposer extends MessageComposer {
    private final CraftingRecipe recipe;

    public CraftingRecipeComposer(CraftingRecipe craftingRecipe) {
        this.recipe = craftingRecipe;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CraftingRecipeComposer);
        this.response.appendInt(Integer.valueOf(this.recipe.getIngredients().size()));
        for (Map.Entry entry : this.recipe.getIngredients().entrySet()) {
            this.response.appendInt((Integer) entry.getValue());
            this.response.appendString(((Item) entry.getKey()).getName());
        }
        return this.response;
    }
}
