package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolIssueChatlogType.class */
public enum ModToolIssueChatlogType {
    NORMAL(0),
    CHAT(1),
    IM(2),
    FORUM_THREAD(3),
    FORUM_COMMENT(4),
    SELFIE(5),
    PHOTO(6);

    private int type;

    ModToolIssueChatlogType(int i) {
        this.type = i;
    }

    public int getType() {
        return this.type;
    }
}
