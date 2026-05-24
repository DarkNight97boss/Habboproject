package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolSanctionItem.class */
public class ModToolSanctionItem {
    public int id;
    public int habboId;
    public int sanctionLevel;
    public int probationTimestamp;
    public boolean isMuted;
    public int muteDuration;
    public int tradeLockedUntil;
    public String reason;

    public ModToolSanctionItem(int i, int i2, int i3, int i4, boolean z, int i5, int i6, String str) {
        this.id = i;
        this.habboId = i2;
        this.sanctionLevel = i3;
        this.probationTimestamp = i4;
        this.isMuted = z;
        this.muteDuration = i5;
        this.tradeLockedUntil = i6;
        this.reason = str;
    }
}
