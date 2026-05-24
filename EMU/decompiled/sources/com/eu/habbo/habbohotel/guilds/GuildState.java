package com.eu.habbo.habbohotel.guilds;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/GuildState.class */
public enum GuildState {
    OPEN(0),
    EXCLUSIVE(1),
    CLOSED(2),
    LARGE(3),
    LARGE_CLOSED(4);

    public final int state;

    GuildState(int i) {
        this.state = i;
    }

    public static GuildState valueOf(int i) {
        try {
            return values()[i];
        } catch (Exception e) {
            return OPEN;
        }
    }
}
