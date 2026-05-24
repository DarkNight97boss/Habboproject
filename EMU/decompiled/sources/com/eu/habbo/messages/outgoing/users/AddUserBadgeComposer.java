package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/AddUserBadgeComposer.class */
public class AddUserBadgeComposer extends MessageComposer {
    private final HabboBadge badge;

    public AddUserBadgeComposer(HabboBadge habboBadge) {
        this.badge = habboBadge;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AddUserBadgeComposer);
        this.response.appendInt(Integer.valueOf(this.badge.getId()));
        this.response.appendString(this.badge.getCode());
        return this.response;
    }
}
