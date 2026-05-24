package com.eu.habbo.habbohotel.users.clothingvalidation;

import java.util.TreeMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/clothingvalidation/FiguredataSettype.class */
public class FiguredataSettype {
    public String type;
    public int paletteId;
    public boolean mandatoryMale0;
    public boolean mandatoryFemale0;
    public boolean mandatoryMale1;
    public boolean mandatoryFemale1;
    public TreeMap<Integer, FiguredataSettypeSet> sets = new TreeMap<>();

    public FiguredataSettype(String str, int i, boolean z, boolean z2, boolean z3, boolean z4) {
        this.type = str;
        this.paletteId = i;
        this.mandatoryMale0 = z;
        this.mandatoryFemale0 = z2;
        this.mandatoryMale1 = z3;
        this.mandatoryFemale1 = z4;
    }

    public void addSet(FiguredataSettypeSet figuredataSettypeSet) {
        this.sets.put(Integer.valueOf(figuredataSettypeSet.id), figuredataSettypeSet);
    }

    public FiguredataSettypeSet getSet(int i) {
        return this.sets.get(Integer.valueOf(i));
    }

    public FiguredataSettypeSet getFirstSetForGender(String str) {
        for (FiguredataSettypeSet figuredataSettypeSet : this.sets.descendingMap().values()) {
            if (figuredataSettypeSet.gender.equalsIgnoreCase(str) || figuredataSettypeSet.gender.equalsIgnoreCase("u")) {
                if (!figuredataSettypeSet.sellable && figuredataSettypeSet.selectable) {
                    return figuredataSettypeSet;
                }
            }
        }
        if (this.sets.size() > 0) {
            return this.sets.descendingMap().entrySet().iterator().next().getValue();
        }
        return null;
    }

    public FiguredataSettypeSet getFirstNonHCSetForGender(String str) {
        for (FiguredataSettypeSet figuredataSettypeSet : this.sets.descendingMap().values()) {
            if (figuredataSettypeSet.gender.equalsIgnoreCase(str) || figuredataSettypeSet.gender.equalsIgnoreCase("u")) {
                if (!figuredataSettypeSet.club && !figuredataSettypeSet.sellable && figuredataSettypeSet.selectable) {
                    return figuredataSettypeSet;
                }
            }
        }
        return getFirstSetForGender(str);
    }
}
