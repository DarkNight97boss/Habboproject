package com.eu.habbo.messages.outgoing.crafting;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/crafting/CraftingRecipesAvailableComposer.class */
public class CraftingRecipesAvailableComposer extends MessageComposer {
    private final int count;
    private final boolean found;

    public CraftingRecipesAvailableComposer(int i, boolean z) {
        this.count = i;
        this.found = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CraftingComposerFour);
        this.response.appendInt(Integer.valueOf((this.found ? -1 : 0) + this.count));
        this.response.appendBoolean(Boolean.valueOf(this.found));
        return this.response;
    }
}
