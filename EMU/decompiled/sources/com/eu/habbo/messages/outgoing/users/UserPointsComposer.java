package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserPointsComposer.class */
public class UserPointsComposer extends MessageComposer {
    private final int currentAmount;
    private final int amountAdded;
    private final int type;

    public UserPointsComposer(int i, int i2, int i3) {
        this.currentAmount = i;
        this.amountAdded = i2;
        this.type = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserPointsComposer);
        this.response.appendInt(Integer.valueOf(this.currentAmount));
        this.response.appendInt(Integer.valueOf(this.amountAdded));
        this.response.appendInt(Integer.valueOf(this.type));
        return this.response;
    }
}
