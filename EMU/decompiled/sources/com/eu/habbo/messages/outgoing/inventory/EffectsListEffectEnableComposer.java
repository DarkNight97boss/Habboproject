package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.users.inventory.EffectsComponent;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/EffectsListEffectEnableComposer.class */
public class EffectsListEffectEnableComposer extends MessageComposer {
    public final EffectsComponent.HabboEffect effect;

    public EffectsListEffectEnableComposer(EffectsComponent.HabboEffect habboEffect) {
        this.effect = habboEffect;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.EffectsListEffectEnableComposer);
        this.response.appendInt(Integer.valueOf(this.effect.effect));
        this.response.appendInt(Integer.valueOf(this.effect.duration));
        this.response.appendBoolean(Boolean.valueOf(this.effect.enabled));
        return this.response;
    }
}
