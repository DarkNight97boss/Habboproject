package com.eu.habbo.messages.outgoing.rooms.items.lovelock;

import com.eu.habbo.habbohotel.items.interactions.InteractionLoveLock;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/lovelock/LoveLockFurniFinishedComposer.class */
public class LoveLockFurniFinishedComposer extends MessageComposer {
    private final InteractionLoveLock loveLock;

    public LoveLockFurniFinishedComposer(InteractionLoveLock interactionLoveLock) {
        this.loveLock = interactionLoveLock;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.LoveLockFurniFinishedComposer);
        this.response.appendInt(Integer.valueOf(this.loveLock.getId()));
        return this.response;
    }
}
