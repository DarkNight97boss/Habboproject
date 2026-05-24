package com.eu.habbo.messages.outgoing.quests;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.quests.QuestsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/quests/QuestComposer.class */
public class QuestComposer extends MessageComposer {
    private final QuestsComposer.Quest quest;

    public QuestComposer(QuestsComposer.Quest quest) {
        this.quest = quest;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(230);
        this.response.append(this.quest);
        return this.response;
    }
}
