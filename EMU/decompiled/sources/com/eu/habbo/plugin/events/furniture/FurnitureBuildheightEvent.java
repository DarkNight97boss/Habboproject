package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureBuildheightEvent.class */
public class FurnitureBuildheightEvent extends FurnitureUserEvent {
    public final double oldHeight;
    public final double newHeight;
    private double updatedHeight;
    private boolean changedHeight;

    public FurnitureBuildheightEvent(HabboItem habboItem, Habbo habbo, double d, double d2) {
        super(habboItem, habbo);
        this.changedHeight = false;
        this.oldHeight = d;
        this.newHeight = d2;
    }

    public void setNewHeight(double d) {
        this.updatedHeight = d;
        this.changedHeight = true;
    }

    public boolean hasChangedHeight() {
        return this.changedHeight;
    }

    public double getUpdatedHeight() {
        return this.updatedHeight;
    }
}
