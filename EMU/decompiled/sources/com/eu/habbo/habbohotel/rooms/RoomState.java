package com.eu.habbo.habbohotel.rooms;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomState.class */
public enum RoomState {
    OPEN(0),
    LOCKED(1),
    PASSWORD(2),
    INVISIBLE(3);

    private final int state;

    RoomState(int i) {
        this.state = i;
    }

    public int getState() {
        return this.state;
    }
}
