package com.eu.habbo.habbohotel.rooms;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomUserRotation.class */
public enum RoomUserRotation {
    NORTH(0),
    NORTH_EAST(1),
    EAST(2),
    SOUTH_EAST(3),
    SOUTH(4),
    SOUTH_WEST(5),
    WEST(6),
    NORTH_WEST(7);

    private final int direction;

    RoomUserRotation(int i) {
        this.direction = i;
    }

    public static RoomUserRotation fromValue(int i) {
        int i2 = i % 8;
        for (RoomUserRotation roomUserRotation : values()) {
            if (roomUserRotation.getValue() == i2) {
                return roomUserRotation;
            }
        }
        return NORTH;
    }

    public static RoomUserRotation counterClockwise(RoomUserRotation roomUserRotation) {
        return fromValue(roomUserRotation.getValue() + 7);
    }

    public static RoomUserRotation clockwise(RoomUserRotation roomUserRotation) {
        return fromValue(roomUserRotation.getValue() + 9);
    }

    public int getValue() {
        return this.direction;
    }

    public RoomUserRotation getOpposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case NORTH_EAST:
                return SOUTH_WEST;
            case EAST:
                return WEST;
            case SOUTH_EAST:
                return NORTH_WEST;
            case SOUTH:
                return NORTH;
            case SOUTH_WEST:
                return NORTH_EAST;
            case WEST:
                return EAST;
            case NORTH_WEST:
                return SOUTH_EAST;
            default:
                return null;
        }
    }
}
