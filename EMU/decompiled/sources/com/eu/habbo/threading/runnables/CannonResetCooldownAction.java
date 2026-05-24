package com.eu.habbo.threading.runnables;

import com.eu.habbo.habbohotel.items.interactions.InteractionCannon;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/CannonResetCooldownAction.class */
public class CannonResetCooldownAction implements Runnable {
    private final InteractionCannon cannon;

    public CannonResetCooldownAction(InteractionCannon interactionCannon) {
        this.cannon = interactionCannon;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.cannon != null) {
            this.cannon.cooldown = false;
        }
    }
}
