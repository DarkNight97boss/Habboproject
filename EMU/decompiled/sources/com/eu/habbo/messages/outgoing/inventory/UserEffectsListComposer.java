package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.inventory.EffectsComponent;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Collection;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/UserEffectsListComposer.class */
public class UserEffectsListComposer extends MessageComposer {
    public final Habbo habbo;
    public final Collection<EffectsComponent.HabboEffect> effects;

    public UserEffectsListComposer(Habbo habbo, Collection<EffectsComponent.HabboEffect> collection) {
        this.habbo = habbo;
        this.effects = collection;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserEffectsListComposer);
        if (this.habbo == null || this.habbo.getInventory() == null || this.habbo.getInventory().getEffectsComponent() == null || this.habbo.getInventory().getEffectsComponent().effects == null) {
            this.response.appendInt((Integer) 0);
        } else {
            synchronized (this.habbo.getInventory().getEffectsComponent().effects) {
                this.response.appendInt(Integer.valueOf(this.effects.size()));
                for (EffectsComponent.HabboEffect habboEffect : this.effects) {
                    this.response.appendInt(Integer.valueOf(habboEffect.effect));
                    this.response.appendInt((Integer) 0);
                    this.response.appendInt(Integer.valueOf(habboEffect.duration > 0 ? habboEffect.duration : Integer.MAX_VALUE));
                    this.response.appendInt(Integer.valueOf(habboEffect.duration > 0 ? habboEffect.total - (habboEffect.isActivated() ? 1 : 0) : 0));
                    if (habboEffect.isActivated() || habboEffect.duration <= 0) {
                        this.response.appendInt(Integer.valueOf(habboEffect.duration > 0 ? (Emulator.getIntUnixTimestamp() - habboEffect.activationTimestamp) + habboEffect.duration : 0));
                    } else {
                        this.response.appendInt((Integer) 0);
                    }
                    this.response.appendBoolean(Boolean.valueOf(habboEffect.duration <= 0));
                }
            }
        }
        return this.response;
    }
}
