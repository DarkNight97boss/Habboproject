package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserAchievementScoreComposer.class */
public class UserAchievementScoreComposer extends MessageComposer {
    private final Habbo habbo;

    public UserAchievementScoreComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserAchievementScoreComposer);
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().getAchievementScore()));
        return this.response;
    }
}
