package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureDiceRolledEvent.class */
public class FurnitureDiceRolledEvent extends FurnitureUserEvent {
    public int result;

    public FurnitureDiceRolledEvent(HabboItem habboItem, Habbo habbo, int i) {
        super(habboItem, habbo);
        this.result = i;
    }
}
