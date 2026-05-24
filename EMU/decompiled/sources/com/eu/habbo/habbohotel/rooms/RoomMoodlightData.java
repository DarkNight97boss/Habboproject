package com.eu.habbo.habbohotel.rooms;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomMoodlightData.class */
public class RoomMoodlightData {
    private int id;
    private boolean enabled;
    private boolean backgroundOnly;
    private String color;
    private int intensity;

    public RoomMoodlightData(int i, boolean z, boolean z2, String str, int i2) {
        this.id = i;
        this.enabled = z;
        this.backgroundOnly = z2;
        this.color = str;
        this.intensity = i2;
    }

    public static RoomMoodlightData fromString(String str) {
        String[] strArrSplit = str.split(",");
        return strArrSplit.length == 5 ? new RoomMoodlightData(Integer.valueOf(strArrSplit[1]).intValue(), strArrSplit[0].equalsIgnoreCase("2"), strArrSplit[2].equalsIgnoreCase("2"), strArrSplit[3], Integer.valueOf(strArrSplit[4]).intValue()) : new RoomMoodlightData(1, true, true, "#000000", 255);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isBackgroundOnly() {
        return this.backgroundOnly;
    }

    public void setBackgroundOnly(boolean z) {
        this.backgroundOnly = z;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String str) {
        this.color = str;
    }

    public int getIntensity() {
        return this.intensity;
    }

    public void setIntensity(int i) {
        this.intensity = i;
    }

    public String toString() {
        return (this.enabled ? 2 : 1) + "," + this.id + "," + (this.backgroundOnly ? 2 : 1) + "," + this.color + "," + this.intensity;
    }
}
