package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureStackHeightEvent.class */
public class FurnitureStackHeightEvent extends Event {
    public final short x;
    public final short y;
    public final Room room;
    private boolean pluginHelper = false;
    private Double height = Double.valueOf(0.0d);

    public FurnitureStackHeightEvent(short s, short s2, Room room) {
        this.x = s;
        this.y = s2;
        this.room = room;
    }

    public void setPluginHelper(boolean z) {
        this.pluginHelper = z;
    }

    public boolean hasPluginHelper() {
        return this.pluginHelper;
    }

    public void setHeight(Double d) {
        this.height = d;
    }

    public Double getHeight() {
        return this.height;
    }
}
