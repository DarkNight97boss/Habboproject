package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.users.inventory.EffectsComponent;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/EffectsListRemoveComposer.class */
public class EffectsListRemoveComposer extends MessageComposer {
    public final EffectsComponent.HabboEffect effect;

    public EffectsListRemoveComposer(EffectsComponent.HabboEffect habboEffect) {
        this.effect = habboEffect;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(2228);
        this.response.appendInt(Integer.valueOf(this.effect.effect));
        return this.response;
    }
}
