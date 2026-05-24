package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolBanType.class */
public enum ModToolBanType {
    ACCOUNT("account"),
    MACHINE("machine"),
    SUPER("super"),
    IP("ip"),
    UNKNOWN("???");

    private final String type;

    ModToolBanType(String str) {
        this.type = str;
    }

    public static ModToolBanType fromString(String str) {
        for (ModToolBanType modToolBanType : values()) {
            if (modToolBanType.type.equalsIgnoreCase(str)) {
                return modToolBanType;
            }
        }
        return UNKNOWN;
    }

    public String getType() {
        return this.type;
    }
}
