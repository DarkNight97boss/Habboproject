package com.eu.habbo.habbohotel.modtool;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolCategory.class */
public class ModToolCategory {
    private final String name;
    private final TIntObjectMap<ModToolPreset> presets = TCollections.synchronizedMap(new TIntObjectHashMap());

    public ModToolCategory(String str) {
        this.name = str;
    }

    public void addPreset(ModToolPreset modToolPreset) {
        this.presets.put(modToolPreset.id, modToolPreset);
    }

    public TIntObjectMap<ModToolPreset> getPresets() {
        return this.presets;
    }

    public String getName() {
        return this.name;
    }
}
