package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomTradeUser.class */
public class RoomTradeUser {
    private final Habbo habbo;
    private final THashSet<HabboItem> items;
    private int userId;
    private boolean accepted;
    private boolean confirmed;

    public RoomTradeUser(Habbo habbo) {
        this.habbo = habbo;
        if (this.habbo != null) {
            this.userId = this.habbo.getHabboInfo().getId();
        }
        this.accepted = false;
        this.confirmed = false;
        this.items = new THashSet<>();
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int i) {
        this.userId = i;
    }

    public Habbo getHabbo() {
        return this.habbo;
    }

    public boolean getAccepted() {
        return this.accepted;
    }

    public void setAccepted(boolean z) {
        this.accepted = z;
    }

    public boolean getConfirmed() {
        return this.confirmed;
    }

    public void confirm() {
        this.confirmed = true;
    }

    public void addItem(HabboItem habboItem) {
        this.items.add(habboItem);
    }

    public HabboItem getItem(int i) {
        TObjectHashIterator it = this.items.iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getId() == i) {
                return habboItem;
            }
        }
        return null;
    }

    public THashSet<HabboItem> getItems() {
        return this.items;
    }

    public void putItemsIntoInventory() {
        this.habbo.getInventory().getItemsComponent().addItems(this.items);
    }

    public void clearItems() {
        this.items.clear();
    }
}
