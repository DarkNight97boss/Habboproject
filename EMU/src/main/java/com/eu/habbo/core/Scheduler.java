package com.eu.habbo.core;

import com.eu.habbo.Emulator;

public class Scheduler implements Runnable {
    protected boolean disposed;
    protected int interval;

    public Scheduler(int interval) {
        this.interval = interval;
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public void setDisposed(boolean disposed) {
        this.disposed = disposed;
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    // Fix audit (R5): ogni (ri)avvio incrementa la generazione; i tick della catena
    // precedente (già accodati nell'executor) si accorgono di essere stantii ed escono,
    // così enable→disable→enable non crea catene duplicate che pagano doppio.
    protected volatile int generation = 0;

    /** Riavvia la catena senza eseguire subito il payout. */
    public void restart() {
        this.disposed = false;
        this.generation++;
        final int gen = this.generation;
        Emulator.getThreading().run(() -> { if (gen == this.generation && !this.disposed) this.run(); }, this.interval * 1000L);
    }

    @Override
    public void run() {
        if (this.disposed)
            return;

        final int gen = this.generation;
        Emulator.getThreading().run(() -> { if (gen == this.generation && !this.disposed) this.run(); }, this.interval * 1000L);
    }
}