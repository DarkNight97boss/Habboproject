package com.eu.habbo.habbohotel.wired;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/WiredMatchFurniSetting.class */
public class WiredMatchFurniSetting {
    public final int item_id;
    public final String state;
    public final int rotation;
    public final int x;
    public final int y;

    public WiredMatchFurniSetting(int i, String str, int i2, int i3, int i4) {
        this.item_id = i;
        this.state = str.replace("\t\t\t", " ");
        this.rotation = i2;
        this.x = i3;
        this.y = i4;
    }

    public String toString() {
        return toString(true);
    }

    public String toString(boolean z) {
        return this.item_id + "-" + ((this.state.isEmpty() || !z) ? " " : this.state) + "-" + this.rotation + "-" + this.x + "-" + this.y;
    }
}
