package com.eu.habbo.habbohotel.catalog.marketplace;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/catalog/marketplace/MarketPlaceState.class */
public enum MarketPlaceState {
    OPEN(1),
    SOLD(2),
    CLOSED(3);

    private final int state;

    MarketPlaceState(int i) {
        this.state = i;
    }

    public static MarketPlaceState getType(int i) {
        switch (i) {
            case 1:
                return OPEN;
            case 2:
                return SOLD;
            case 3:
                return CLOSED;
            default:
                return CLOSED;
        }
    }

    public int getState() {
        return this.state;
    }
}
