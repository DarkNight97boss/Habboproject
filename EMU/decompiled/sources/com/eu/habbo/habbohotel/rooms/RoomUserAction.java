package com.eu.habbo.habbohotel.rooms;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomUserAction.class */
public enum RoomUserAction {
    NONE(0),
    WAVE(1),
    BLOW_KISS(2),
    LAUGH(3),
    UNKNOWN(4),
    IDLE(5),
    JUMP(6),
    THUMB_UP(7);

    private final int action;

    RoomUserAction(int i) {
        this.action = i;
    }

    public static RoomUserAction fromValue(int i) {
        for (RoomUserAction roomUserAction : values()) {
            if (roomUserAction.getAction() == i) {
                return roomUserAction;
            }
        }
        return NONE;
    }

    public int getAction() {
        return this.action;
    }
}
