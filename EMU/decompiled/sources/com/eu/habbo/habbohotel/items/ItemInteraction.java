package com.eu.habbo.habbohotel.items;

import com.eu.habbo.habbohotel.users.HabboItem;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/ItemInteraction.class */
public class ItemInteraction {
    private final String name;
    private final Class<? extends HabboItem> type;

    public ItemInteraction(String str, Class<? extends HabboItem> cls) {
        this.name = str;
        this.type = cls;
    }

    public Class<? extends HabboItem> getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
}
