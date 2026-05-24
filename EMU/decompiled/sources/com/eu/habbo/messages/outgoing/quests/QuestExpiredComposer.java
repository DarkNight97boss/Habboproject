package com.eu.habbo.messages.outgoing.quests;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/quests/QuestExpiredComposer.class */
public class QuestExpiredComposer extends MessageComposer {
    private final boolean expired;

    public QuestExpiredComposer(boolean z) {
        this.expired = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3027);
        this.response.appendBoolean(Boolean.valueOf(this.expired));
        return this.response;
    }
}
