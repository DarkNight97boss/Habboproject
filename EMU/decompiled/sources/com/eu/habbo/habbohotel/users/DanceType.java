package com.eu.habbo.habbohotel.users;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/DanceType.class */
public enum DanceType {
    NONE(0),
    HAB_HOP(1),
    POGO_MOGO(2),
    DUCK_FUNK(3),
    THE_ROLLIE(4);

    private final int type;

    DanceType(int i) {
        this.type = i;
    }

    public int getType() {
        return this.type;
    }
}
