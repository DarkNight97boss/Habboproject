package com.eu.habbo.habbohotel.items;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/FurnitureType.class */
public enum FurnitureType {
    FLOOR("S"),
    WALL("I"),
    EFFECT("E"),
    BADGE("B"),
    ROBOT("R"),
    HABBO_CLUB("H"),
    PET("P");

    public final String code;

    FurnitureType(String str) {
        this.code = str;
    }

    public static FurnitureType fromString(String str) {
        switch (str.toUpperCase()) {
            case "S":
                return FLOOR;
            case "I":
                return WALL;
            case "E":
                return EFFECT;
            case "B":
                return BADGE;
            case "R":
                return ROBOT;
            case "H":
                return HABBO_CLUB;
            case "P":
                return PET;
            default:
                return FLOOR;
        }
    }
}
