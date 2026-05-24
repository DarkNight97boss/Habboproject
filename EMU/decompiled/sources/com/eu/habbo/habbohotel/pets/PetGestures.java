package com.eu.habbo.habbohotel.pets;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/PetGestures.class */
public enum PetGestures {
    THIRSTY("thr"),
    TIRED("trd"),
    PLAYFULL("plf"),
    HUNGRY("hng"),
    SAD("sad"),
    HAPPY("sml"),
    QUESTION("que"),
    LVLUP("exp"),
    LOVE("lov"),
    WARNING("und"),
    ENERGY("nrg");

    private final String key;

    PetGestures(String str) {
        this.key = str;
    }

    public String getKey() {
        return this.key;
    }
}
