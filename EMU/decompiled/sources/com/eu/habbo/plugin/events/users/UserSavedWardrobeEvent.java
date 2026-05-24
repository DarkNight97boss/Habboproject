package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.inventory.WardrobeComponent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserSavedWardrobeEvent.class */
public class UserSavedWardrobeEvent extends UserEvent {
    public final WardrobeComponent.WardrobeItem wardrobeItem;

    public UserSavedWardrobeEvent(Habbo habbo, WardrobeComponent.WardrobeItem wardrobeItem) {
        super(habbo);
        this.wardrobeItem = wardrobeItem;
    }
}
