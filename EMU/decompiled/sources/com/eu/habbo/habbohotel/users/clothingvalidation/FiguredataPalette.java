package com.eu.habbo.habbohotel.users.clothingvalidation;

import java.util.TreeMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/clothingvalidation/FiguredataPalette.class */
public class FiguredataPalette {
    public int id;
    public TreeMap<Integer, FiguredataPaletteColor> colors = new TreeMap<>();

    public FiguredataPalette(int i) {
        this.id = i;
    }

    public void addColor(FiguredataPaletteColor figuredataPaletteColor) {
        this.colors.put(Integer.valueOf(figuredataPaletteColor.id), figuredataPaletteColor);
    }

    public FiguredataPaletteColor getColor(int i) {
        return this.colors.get(Integer.valueOf(i));
    }

    public FiguredataPaletteColor getFirstNonHCColor() {
        for (FiguredataPaletteColor figuredataPaletteColor : this.colors.values()) {
            if (!figuredataPaletteColor.club && figuredataPaletteColor.selectable) {
                return figuredataPaletteColor;
            }
        }
        if (this.colors.size() > 0) {
            return this.colors.entrySet().iterator().next().getValue();
        }
        return null;
    }
}
