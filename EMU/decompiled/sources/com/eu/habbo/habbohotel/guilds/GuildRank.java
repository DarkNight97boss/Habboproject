package com.eu.habbo.habbohotel.guilds;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/GuildRank.class */
public enum GuildRank {
    OWNER(0),
    ADMIN(1),
    MEMBER(2),
    REQUESTED(3),
    DELETED(4);

    public final int type;

    GuildRank(int i) {
        this.type = i;
    }

    public static GuildRank getRank(int i) {
        try {
            return values()[i];
        } catch (Exception e) {
            return MEMBER;
        }
    }
}
