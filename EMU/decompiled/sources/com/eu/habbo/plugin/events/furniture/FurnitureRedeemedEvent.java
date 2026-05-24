package com.eu.habbo.plugin.events.furniture;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/furniture/FurnitureRedeemedEvent.class */
public class FurnitureRedeemedEvent extends FurnitureUserEvent {
    public static final int CREDITS = -1;
    public static final int PIXELS = 0;
    public static final int DIAMONDS = 5;
    public final int amount;
    public final int currencyID;

    public FurnitureRedeemedEvent(HabboItem habboItem, Habbo habbo, int i, int i2) {
        super(habboItem, habbo);
        this.amount = i;
        this.currencyID = i2;
    }
}
