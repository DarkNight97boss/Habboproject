package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionPostIt;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/PostItDataComposer.class */
public class PostItDataComposer extends MessageComposer {
    private final InteractionPostIt postIt;

    public PostItDataComposer(InteractionPostIt interactionPostIt) {
        this.postIt = interactionPostIt;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        if (this.postIt.getExtradata().isEmpty() || this.postIt.getExtradata().length() < 6) {
            this.postIt.setExtradata("FFFF33");
        }
        this.response.init(Outgoing.PostItDataComposer);
        this.response.appendString(this.postIt.getId() + Emulator.PREVIEW);
        this.response.appendString(this.postIt.getExtradata());
        return this.response;
    }
}
