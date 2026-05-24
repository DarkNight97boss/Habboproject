package com.eu.habbo.habbohotel.rooms;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomUnitType.class */
public enum RoomUnitType {
    USER(1),
    BOT(4),
    PET(2),
    UNKNOWN(3);

    private final int typeId;

    RoomUnitType(int i) {
        this.typeId = i;
    }

    public int getTypeId() {
        return this.typeId;
    }
}
