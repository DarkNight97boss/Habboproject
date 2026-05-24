package com.eu.habbo.habbohotel.items;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/RedeemableSubscriptionType.class */
public enum RedeemableSubscriptionType {
    HABBO_CLUB("hc"),
    BUILDERS_CLUB("bc");

    public final String subscriptionType;

    RedeemableSubscriptionType(String str) {
        this.subscriptionType = str;
    }

    public static RedeemableSubscriptionType fromString(String str) {
        if (str == null) {
            return null;
        }
        switch (str) {
            case "hc":
                return HABBO_CLUB;
            case "bc":
                return BUILDERS_CLUB;
            default:
                return null;
        }
    }
}
