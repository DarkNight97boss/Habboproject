package com.eu.habbo.habbohotel.wired;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/WiredGiveRewardItem.class */
public class WiredGiveRewardItem {
    public final int id;
    public final boolean badge;
    public final String data;
    public final int probability;

    public WiredGiveRewardItem(int i, boolean z, String str, int i2) {
        this.id = i;
        this.badge = z;
        this.data = str;
        this.probability = i2;
    }

    public WiredGiveRewardItem(String str) {
        String[] strArrSplit = str.split(",");
        this.id = Integer.valueOf(strArrSplit[0]).intValue();
        this.badge = strArrSplit[1].equalsIgnoreCase("0");
        this.data = strArrSplit[2];
        this.probability = Integer.valueOf(strArrSplit[3]).intValue();
    }

    public String toString() {
        return this.id + "," + (this.badge ? 0 : 1) + "," + this.data + "," + this.probability;
    }

    public String wiredString() {
        return (this.badge ? 0 : 1) + "," + this.data + "," + this.probability;
    }
}
