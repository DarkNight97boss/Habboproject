package com.eu.habbo.habbohotel.navigation;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/navigation/ListMode.class */
public enum ListMode {
    LIST(0),
    THUMBNAILS(1),
    FORCED_THUNBNAILS(2);

    public final int type;

    ListMode(int i) {
        this.type = i;
    }

    public static ListMode fromType(int i) {
        for (ListMode listMode : values()) {
            if (listMode.type == i) {
                return listMode;
            }
        }
        return LIST;
    }
}
