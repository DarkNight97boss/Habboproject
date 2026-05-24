package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.users.inventory.EffectsComponent;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/EffectsListAddComposer.class */
public class EffectsListAddComposer extends MessageComposer {
    public final EffectsComponent.HabboEffect effect;

    public EffectsListAddComposer(EffectsComponent.HabboEffect habboEffect) {
        this.effect = habboEffect;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.EffectsListAddComposer);
        this.response.appendInt(Integer.valueOf(this.effect.effect));
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(this.effect.duration > 0 ? this.effect.duration : Integer.MAX_VALUE));
        this.response.appendBoolean(Boolean.valueOf(this.effect.duration <= 0));
        return this.response;
    }
}
