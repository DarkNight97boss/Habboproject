package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/Scheduler.class */
public class Scheduler implements Runnable {
    protected boolean disposed;
    protected int interval;

    public Scheduler(int i) {
        this.interval = i;
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public void setDisposed(boolean z) {
        this.disposed = z;
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int i) {
        this.interval = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.disposed) {
            return;
        }
        Emulator.getThreading().run(this, this.interval * Outgoing.CraftableProductsComposer);
    }
}
