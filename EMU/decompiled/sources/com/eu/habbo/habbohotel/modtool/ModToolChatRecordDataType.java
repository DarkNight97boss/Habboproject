package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolChatRecordDataType.class */
public enum ModToolChatRecordDataType {
    UNKNOWN(0),
    ROOM_TOOL(1),
    IM_SESSION(2),
    FORUM_THREAD(3),
    FORUM_MESSAGE(4),
    SELFIE(5),
    PHOTO_REPORT(6);

    public final int type;

    ModToolChatRecordDataType(int i) {
        this.type = i;
    }
}
