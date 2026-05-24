package com.eu.habbo.messages.outgoing.wired;

import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/wired/WiredConditionDataComposer.class */
public class WiredConditionDataComposer extends MessageComposer {
    private final InteractionWiredCondition condition;
    private final Room room;

    public WiredConditionDataComposer(InteractionWiredCondition interactionWiredCondition, Room room) {
        this.condition = interactionWiredCondition;
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.WiredConditionDataComposer);
        this.condition.serializeWiredData(this.response, this.room);
        this.condition.needsUpdate(true);
        return this.response;
    }
}
