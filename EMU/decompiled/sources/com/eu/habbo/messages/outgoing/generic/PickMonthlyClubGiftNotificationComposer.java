package com.eu.habbo.messages.outgoing.generic;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/PickMonthlyClubGiftNotificationComposer.class */
public class PickMonthlyClubGiftNotificationComposer extends MessageComposer {
    private final int count;

    public PickMonthlyClubGiftNotificationComposer(int i) {
        this.count = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PickMonthlyClubGiftNotificationComposer);
        this.response.appendInt(Integer.valueOf(this.count));
        return this.response;
    }
}
