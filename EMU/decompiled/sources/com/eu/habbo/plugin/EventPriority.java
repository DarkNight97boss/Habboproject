package com.eu.habbo.plugin;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/EventPriority.class */
public enum EventPriority {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4),
    MONITOR(5);

    private final int slot;

    EventPriority(int i) {
        this.slot = i;
    }

    public int getSlot() {
        return this.slot;
    }
}
