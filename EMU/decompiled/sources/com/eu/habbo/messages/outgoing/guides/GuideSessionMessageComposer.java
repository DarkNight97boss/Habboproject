package com.eu.habbo.messages.outgoing.guides;

import com.eu.habbo.habbohotel.guides.GuideChatMessage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guides/GuideSessionMessageComposer.class */
public class GuideSessionMessageComposer extends MessageComposer {
    private final GuideChatMessage message;

    public GuideSessionMessageComposer(GuideChatMessage guideChatMessage) {
        this.message = guideChatMessage;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuideSessionMessageComposer);
        this.response.appendString(this.message.message);
        this.response.appendInt(Integer.valueOf(this.message.userId));
        return this.response;
    }
}
