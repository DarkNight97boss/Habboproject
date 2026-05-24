package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureEvent.class */
public abstract class FurnitureEvent extends Event {
    public final HabboItem furniture;

    public FurnitureEvent(HabboItem habboItem) {
        this.furniture = habboItem;
    }
}
