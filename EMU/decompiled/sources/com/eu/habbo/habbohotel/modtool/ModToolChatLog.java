package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolChatLog.class */
public class ModToolChatLog implements Comparable<ModToolChatLog> {
    public final int timestamp;
    public final int habboId;
    public final String username;
    public final String message;
    public final boolean highlighted;

    public ModToolChatLog(int i, int i2, String str, String str2) {
        this.timestamp = i;
        this.habboId = i2;
        this.username = str;
        this.message = str2;
        this.highlighted = false;
    }

    public ModToolChatLog(int i, int i2, String str, String str2, boolean z) {
        this.timestamp = i;
        this.habboId = i2;
        this.username = str;
        this.message = str2;
        this.highlighted = z;
    }

    @Override // java.lang.Comparable
    public int compareTo(ModToolChatLog modToolChatLog) {
        return modToolChatLog.timestamp - this.timestamp;
    }
}
