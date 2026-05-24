package com.eu.habbo.messages.outgoing.wired;

import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/wired/WiredTriggerDataComposer.class */
public class WiredTriggerDataComposer extends MessageComposer {
    private final InteractionWiredTrigger trigger;
    private final Room room;

    public WiredTriggerDataComposer(InteractionWiredTrigger interactionWiredTrigger, Room room) {
        this.trigger = interactionWiredTrigger;
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.WiredTriggerDataComposer);
        this.trigger.serializeWiredData(this.response, this.room);
        this.trigger.needsUpdate(true);
        return this.response;
    }
}
