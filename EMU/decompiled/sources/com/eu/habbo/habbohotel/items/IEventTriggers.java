package com.eu.habbo.habbohotel.items;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/IEventTriggers.class */
public interface IEventTriggers {
    void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception;

    void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception;

    void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception;
}
