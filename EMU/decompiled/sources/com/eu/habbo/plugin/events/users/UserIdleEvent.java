package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserIdleEvent.class */
public class UserIdleEvent extends UserEvent {
    public final IdleReason reason;
    public boolean idle;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserIdleEvent$IdleReason.class */
    public enum IdleReason {
        ACTION,
        DANCE,
        TIMEOUT,
        WALKED,
        TALKED
    }

    public UserIdleEvent(Habbo habbo, IdleReason idleReason, boolean z) {
        super(habbo);
        this.reason = idleReason;
        this.idle = z;
    }
}
