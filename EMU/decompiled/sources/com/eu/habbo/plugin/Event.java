package com.eu.habbo.plugin;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/Event.class */
public abstract class Event {
    private boolean cancelled = false;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean z) {
        this.cancelled = z;
    }
}
