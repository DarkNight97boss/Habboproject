package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserPurchasePictureEvent.class */
public class UserPurchasePictureEvent extends UserEvent {
    public String url;
    public int roomId;
    public int timestamp;

    public UserPurchasePictureEvent(Habbo habbo, String str, int i, int i2) {
        super(habbo);
        this.url = str;
        this.roomId = i;
        this.timestamp = i2;
    }
}
