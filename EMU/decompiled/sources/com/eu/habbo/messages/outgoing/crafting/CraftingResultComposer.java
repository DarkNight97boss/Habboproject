package com.eu.habbo.messages.outgoing.crafting;

import com.eu.habbo.habbohotel.crafting.CraftingRecipe;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/crafting/CraftingResultComposer.class */
public class CraftingResultComposer extends MessageComposer {
    private final CraftingRecipe recipe;
    private final boolean succes;

    public CraftingResultComposer(CraftingRecipe craftingRecipe) {
        this.recipe = craftingRecipe;
        this.succes = this.recipe != null;
    }

    public CraftingResultComposer(CraftingRecipe craftingRecipe, boolean z) {
        this.recipe = craftingRecipe;
        this.succes = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CraftingResultComposer);
        this.response.appendBoolean(Boolean.valueOf(this.succes));
        if (this.recipe != null) {
            this.response.appendString(this.recipe.getName());
            this.response.appendString(this.recipe.getReward().getName());
        }
        return this.response;
    }
}
