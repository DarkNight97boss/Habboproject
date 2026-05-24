package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserSavedMottoEvent.class */
public class UserSavedMottoEvent extends UserEvent {
    public final String oldMotto;
    public String newMotto;

    public UserSavedMottoEvent(Habbo habbo, String str, String str2) {
        super(habbo);
        this.oldMotto = str;
        this.newMotto = str2;
    }
}
