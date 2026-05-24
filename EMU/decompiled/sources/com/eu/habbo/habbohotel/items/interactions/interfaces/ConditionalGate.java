package com.eu.habbo.habbohotel.items.interactions.interfaces;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/interfaces/ConditionalGate.class */
public interface ConditionalGate {
    void onRejected(RoomUnit roomUnit, Room room, Object[] objArr);
}
