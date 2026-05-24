package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureRoomTonerEvent.class */
public class FurnitureRoomTonerEvent extends FurnitureUserEvent {
    public int hue;
    public int saturation;
    public int brightness;

    public FurnitureRoomTonerEvent(HabboItem habboItem, Habbo habbo, int i, int i2, int i3) {
        super(habboItem, habbo);
        this.hue = i;
        this.saturation = i2;
        this.brightness = i3;
    }
}
