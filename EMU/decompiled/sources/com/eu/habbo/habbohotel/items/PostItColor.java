package com.eu.habbo.habbohotel.items;

import com.eu.habbo.Emulator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/PostItColor.class */
public enum PostItColor {
    BLUE("9CCEFF"),
    GREEN("9CFF9C"),
    PINK("FF9CFF"),
    YELLOW("FFFF33");

    public final String hexColor;

    PostItColor(String str) {
        this.hexColor = str;
    }

    public static boolean isCustomColor(String str) {
        for (PostItColor postItColor : values()) {
            if (postItColor.hexColor.equalsIgnoreCase(str)) {
                return false;
            }
        }
        return true;
    }

    public static PostItColor randomColorNotYellow() {
        return values()[Emulator.getRandom().nextInt(3)];
    }
}
