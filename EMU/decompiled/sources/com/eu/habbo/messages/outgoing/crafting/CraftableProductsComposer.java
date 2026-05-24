package com.eu.habbo.messages.outgoing.crafting;

import com.eu.habbo.habbohotel.crafting.CraftingRecipe;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/crafting/CraftableProductsComposer.class */
public class CraftableProductsComposer extends MessageComposer {
    private final List<CraftingRecipe> recipes;
    private final Collection<Item> ingredients;

    public CraftableProductsComposer(List<CraftingRecipe> list, Collection<Item> collection) {
        this.recipes = list;
        this.ingredients = collection;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CraftableProductsComposer);
        this.response.appendInt(Integer.valueOf(this.recipes.size()));
        for (CraftingRecipe craftingRecipe : this.recipes) {
            this.response.appendString(craftingRecipe.getName());
            this.response.appendString(craftingRecipe.getReward().getName());
        }
        this.response.appendInt(Integer.valueOf(this.ingredients.size()));
        Iterator<Item> it = this.ingredients.iterator();
        while (it.hasNext()) {
            this.response.appendString(it.next().getName());
        }
        return this.response;
    }
}
