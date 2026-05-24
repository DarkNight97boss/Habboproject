package com.eu.habbo.habbohotel.items.interactions.interfaces;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;

public interface ConditionalGate {
   void onRejected(RoomUnit var1, Room var2, Object[] var3);
}
