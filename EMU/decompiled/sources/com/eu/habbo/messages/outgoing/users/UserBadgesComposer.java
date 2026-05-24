package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserBadgesComposer.class */
public class UserBadgesComposer extends MessageComposer {
    private final ArrayList<HabboBadge> badges;
    private final int habbo;

    public UserBadgesComposer(ArrayList<HabboBadge> arrayList, int i) {
        this.badges = arrayList;
        this.habbo = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserBadgesComposer);
        this.response.appendInt(Integer.valueOf(this.habbo));
        synchronized (this.badges) {
            this.response.appendInt(Integer.valueOf(this.badges.size()));
            for (HabboBadge habboBadge : this.badges) {
                this.response.appendInt(Integer.valueOf(habboBadge.getSlot()));
                this.response.appendString(habboBadge.getCode());
            }
        }
        return this.response;
    }
}
