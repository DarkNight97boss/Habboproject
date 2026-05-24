package com.eu.habbo.habbohotel.guilds;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/GuildMembershipStatus.class */
public enum GuildMembershipStatus {
    NOT_MEMBER(0),
    MEMBER(1),
    PENDING(2);

    private int status;

    GuildMembershipStatus(int i) {
        this.status = i;
    }

    public int getStatus() {
        return this.status;
    }
}
