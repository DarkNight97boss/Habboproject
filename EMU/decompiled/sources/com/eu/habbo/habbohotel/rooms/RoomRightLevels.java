package com.eu.habbo.habbohotel.rooms;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomRightLevels.class */
public enum RoomRightLevels {
    NONE(0),
    RIGHTS(1),
    GUILD_RIGHTS(2),
    GUILD_ADMIN(3),
    OWNER(4),
    MODERATOR(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9);

    public final int level;

    RoomRightLevels(int i) {
        this.level = i;
    }

    public boolean equals(RoomRightLevels roomRightLevels) {
        return this.level == roomRightLevels.level;
    }

    public boolean isEqualOrGreaterThan(RoomRightLevels roomRightLevels) {
        return this.level >= roomRightLevels.level;
    }

    public boolean isGreaterThan(RoomRightLevels roomRightLevels) {
        return this.level > roomRightLevels.level;
    }

    public boolean isLessThan(RoomRightLevels roomRightLevels) {
        return this.level < roomRightLevels.level;
    }
}
