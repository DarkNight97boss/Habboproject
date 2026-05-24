package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserPickGiftEvent.class */
public class UserPickGiftEvent extends UserEvent {
    public final int keyA;
    public final int keyB;
    public final int index;

    public UserPickGiftEvent(Habbo habbo, int i, int i2, int i3) {
        super(habbo);
        this.keyA = i;
        this.keyB = i2;
        this.index = i3;
    }
}
