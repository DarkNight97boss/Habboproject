package com.eu.habbo.plugin.events.rooms;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.plugin.events.users.UserEvent;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/rooms/RoomFloorItemsLoadEvent.class */
public class RoomFloorItemsLoadEvent extends UserEvent {
    private THashSet<HabboItem> floorItems;
    private boolean changedFloorItems;

    public RoomFloorItemsLoadEvent(Habbo habbo, THashSet<HabboItem> tHashSet) {
        super(habbo);
        this.floorItems = tHashSet;
        this.changedFloorItems = false;
    }

    public void setFloorItems(THashSet<HabboItem> tHashSet) {
        this.changedFloorItems = true;
        this.floorItems = tHashSet;
    }

    public boolean hasChangedFloorItems() {
        return this.changedFloorItems;
    }

    public THashSet<HabboItem> getFloorItems() {
        return this.floorItems;
    }
}
