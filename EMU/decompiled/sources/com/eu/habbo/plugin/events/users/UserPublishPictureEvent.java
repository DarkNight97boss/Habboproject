package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserPublishPictureEvent.class */
public class UserPublishPictureEvent extends UserEvent {
    public String URL;
    public int timestamp;
    public int roomId;

    public UserPublishPictureEvent(Habbo habbo, String str, int i, int i2) {
        super(habbo);
        this.URL = str;
        this.timestamp = i;
        this.roomId = i2;
    }
}
