package com.eu.habbo.habbohotel.items.interactions.totems;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/totems/TotemPlanetType.class */
public enum TotemPlanetType {
    MOON(0),
    SUN(1),
    EARTH(2);

    public final int type;

    TotemPlanetType(int i) {
        this.type = i;
    }

    public static TotemPlanetType fromInt(int i) {
        for (TotemPlanetType totemPlanetType : values()) {
            if (totemPlanetType.type == i) {
                return totemPlanetType;
            }
        }
        return MOON;
    }
}
