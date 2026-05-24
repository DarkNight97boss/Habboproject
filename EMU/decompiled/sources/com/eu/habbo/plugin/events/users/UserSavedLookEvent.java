package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserSavedLookEvent.class */
public class UserSavedLookEvent extends UserEvent {
    public HabboGender gender;
    public String newLook;

    public UserSavedLookEvent(Habbo habbo, HabboGender habboGender, String str) {
        super(habbo);
        this.gender = habboGender;
        this.newLook = str;
    }
}
