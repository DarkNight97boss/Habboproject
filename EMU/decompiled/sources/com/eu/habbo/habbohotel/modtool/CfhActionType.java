package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/CfhActionType.class */
public enum CfhActionType {
    MODS(0),
    AUTO_REPLY(1),
    AUTO_IGNORE(2),
    GUARDIANS(3);

    public final int type;

    CfhActionType(int i) {
        this.type = i;
    }

    public static CfhActionType get(String str) {
        switch (str) {
            case "auto_reply":
                return AUTO_REPLY;
            case "auto_ignore":
                return AUTO_IGNORE;
            case "guardians":
                return GUARDIANS;
            default:
                return MODS;
        }
    }

    @Override // java.lang.Enum
    public String toString() {
        return name().toLowerCase();
    }
}
