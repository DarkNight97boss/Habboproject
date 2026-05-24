package com.eu.habbo.habbohotel.items.interactions.wired;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/WiredSettings.class */
public class WiredSettings {
    private int[] intParams;
    private String stringParam;
    private int[] furniIds;
    private int stuffTypeSelectionCode;
    private int delay;

    public WiredSettings(int[] iArr, String str, int[] iArr2, int i, int i2) {
        this.furniIds = iArr2;
        this.intParams = iArr;
        this.stringParam = str;
        this.stuffTypeSelectionCode = i;
        this.delay = i2;
    }

    public WiredSettings(int[] iArr, String str, int[] iArr2, int i) {
        this(iArr, str, iArr2, i, 0);
    }

    public int getStuffTypeSelectionCode() {
        return this.stuffTypeSelectionCode;
    }

    public void setStuffTypeSelectionCode(int i) {
        this.stuffTypeSelectionCode = i;
    }

    public int[] getFurniIds() {
        return this.furniIds;
    }

    public void setFurniIds(int[] iArr) {
        this.furniIds = iArr;
    }

    public String getStringParam() {
        return this.stringParam;
    }

    public void setStringParam(String str) {
        this.stringParam = str;
    }

    public int[] getIntParams() {
        return this.intParams;
    }

    public void setIntParams(int[] iArr) {
        this.intParams = iArr;
    }

    public int getDelay() {
        return this.delay;
    }

    public void setDelay(int i) {
        this.delay = i;
    }
}
