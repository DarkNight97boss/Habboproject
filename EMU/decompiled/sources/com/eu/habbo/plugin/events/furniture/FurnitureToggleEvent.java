package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureToggleEvent.class */
public class FurnitureToggleEvent extends FurnitureUserEvent {
    public int state;

    public FurnitureToggleEvent(HabboItem habboItem, Habbo habbo, int i) {
        super(habboItem, habbo);
        this.state = i;
    }
}
