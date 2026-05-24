package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurniturePickedUpEvent.class */
public class FurniturePickedUpEvent extends FurnitureUserEvent {
    public FurniturePickedUpEvent(HabboItem habboItem, Habbo habbo) {
        super(habboItem, habbo);
    }
}
