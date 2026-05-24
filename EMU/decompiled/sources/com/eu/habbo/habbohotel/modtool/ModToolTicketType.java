package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolTicketType.class */
public enum ModToolTicketType {
    NORMAL(1),
    NORMAL_UNKNOWN(2),
    AUTOMATIC(3),
    AUTOMATIC_IM(4),
    GUIDE_SYSTEM(5),
    IM(6),
    ROOM(7),
    PANIC(8),
    GUARDIAN(9),
    AUTOMATIC_HELPER(10),
    DISCUSSION(11),
    SELFIE(12),
    POTATO(13),
    PHOTO(14),
    AMBASSADOR(15);

    private final int type;

    ModToolTicketType(int i) {
        this.type = i;
    }

    public int getType() {
        return this.type;
    }
}
