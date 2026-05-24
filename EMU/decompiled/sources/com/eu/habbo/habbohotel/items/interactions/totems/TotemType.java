package com.eu.habbo.habbohotel.items.interactions.totems;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/totems/TotemType.class */
public enum TotemType {
    NONE(0),
    TROLL(1),
    SNAKE(2),
    BIRD(3);

    public final int type;

    TotemType(int i) {
        this.type = i;
    }

    public static TotemType fromInt(int i) {
        for (TotemType totemType : values()) {
            if (totemType.type == i) {
                return totemType;
            }
        }
        return NONE;
    }
}
