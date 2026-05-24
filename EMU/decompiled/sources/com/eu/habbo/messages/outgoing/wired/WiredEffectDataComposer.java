package com.eu.habbo.messages.outgoing.wired;

import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/wired/WiredEffectDataComposer.class */
public class WiredEffectDataComposer extends MessageComposer {
    private final InteractionWiredEffect effect;
    private final Room room;

    public WiredEffectDataComposer(InteractionWiredEffect interactionWiredEffect, Room room) {
        this.effect = interactionWiredEffect;
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.WiredEffectDataComposer);
        this.effect.serializeWiredData(this.response, this.room);
        this.effect.needsUpdate(true);
        return this.response;
    }
}
