package com.eu.habbo.habbohotel.guilds;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/SettingsState.class */
public enum SettingsState {
    EVERYONE(0),
    MEMBERS(1),
    ADMINS(2),
    OWNER(3);

    public final int state;

    SettingsState(int i) {
        this.state = i;
    }

    public static SettingsState fromValue(int i) {
        switch (i) {
            case 0:
                return EVERYONE;
            case 1:
                return MEMBERS;
            case 2:
                return ADMINS;
            case 3:
                return OWNER;
            default:
                return EVERYONE;
        }
    }
}
