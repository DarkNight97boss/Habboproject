package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureUserEvent.class */
public abstract class FurnitureUserEvent extends FurnitureEvent {
    public final Habbo habbo;

    public FurnitureUserEvent(HabboItem habboItem, Habbo habbo) {
        super(habboItem);
        this.habbo = habbo;
    }
}
