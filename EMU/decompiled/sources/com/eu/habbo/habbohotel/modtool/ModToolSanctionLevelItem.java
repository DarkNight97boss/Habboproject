package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolSanctionLevelItem.class */
public class ModToolSanctionLevelItem {
    public int sanctionLevel;
    public String sanctionType;
    public int sanctionHourLength;
    public int sanctionProbationDays;

    public ModToolSanctionLevelItem(int i, String str, int i2, int i3) {
        this.sanctionLevel = i;
        this.sanctionType = str;
        this.sanctionHourLength = i2;
        this.sanctionProbationDays = i3;
    }
}
