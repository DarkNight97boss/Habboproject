package com.eu.habbo.habbohotel.items.interactions.totems;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/totems/TotemColor.class */
public enum TotemColor {
    NONE(0),
    RED(1),
    YELLOW(2),
    BLUE(3);

    public final int color;

    TotemColor(int i) {
        this.color = i;
    }

    public static TotemColor fromInt(int i) {
        for (TotemColor totemColor : values()) {
            if (totemColor.color == i) {
                return totemColor;
            }
        }
        return NONE;
    }
}
