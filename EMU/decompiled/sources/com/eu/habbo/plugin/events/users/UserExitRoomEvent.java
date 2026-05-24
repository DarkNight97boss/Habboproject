package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserExitRoomEvent.class */
public class UserExitRoomEvent extends UserEvent {
    public final UserExitRoomReason reason;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserExitRoomEvent$UserExitRoomReason.class */
    public enum UserExitRoomReason {
        DOOR(false),
        KICKED_HABBO(false),
        KICKED_IDLE(true),
        TELEPORT(false);

        public final boolean cancellable;

        UserExitRoomReason(boolean z) {
            this.cancellable = z;
        }
    }

    public UserExitRoomEvent(Habbo habbo, UserExitRoomReason userExitRoomReason) {
        super(habbo);
        this.reason = userExitRoomReason;
    }
}
