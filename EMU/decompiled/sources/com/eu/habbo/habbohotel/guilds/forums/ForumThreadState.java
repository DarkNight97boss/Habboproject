package com.eu.habbo.habbohotel.guilds.forums;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/forums/ForumThreadState.class */
public enum ForumThreadState {
    OPEN(0),
    CLOSED(1),
    HIDDEN_BY_STAFF_MEMBER(10),
    HIDDEN_BY_GUILD_ADMIN(20);

    private int stateId;

    ForumThreadState(int i) {
        this.stateId = i;
    }

    public static ForumThreadState fromValue(int i) {
        for (ForumThreadState forumThreadState : values()) {
            if (forumThreadState.stateId == i) {
                return forumThreadState;
            }
        }
        return CLOSED;
    }

    public int getStateId() {
        return this.stateId;
    }
}
