package com.eu.habbo.messages.outgoing.rooms.items.lovelock;

import com.eu.habbo.habbohotel.items.interactions.InteractionLoveLock;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/lovelock/LoveLockFurniStartComposer.class */
public class LoveLockFurniStartComposer extends MessageComposer {
    private final InteractionLoveLock loveLock;

    public LoveLockFurniStartComposer(InteractionLoveLock interactionLoveLock) {
        this.loveLock = interactionLoveLock;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.LoveLockFurniStartComposer);
        this.response.appendInt(Integer.valueOf(this.loveLock.getId()));
        this.response.appendBoolean(true);
        return this.response;
    }
}
