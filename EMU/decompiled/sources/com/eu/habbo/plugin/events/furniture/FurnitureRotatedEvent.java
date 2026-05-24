package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureRotatedEvent.class */
public class FurnitureRotatedEvent extends FurnitureUserEvent {
    public final int oldRotation;

    public FurnitureRotatedEvent(HabboItem habboItem, Habbo habbo, int i) {
        super(habboItem, habbo);
        this.oldRotation = i;
    }
}
