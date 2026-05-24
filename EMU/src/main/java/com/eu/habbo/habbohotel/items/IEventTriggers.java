package com.eu.habbo.habbohotel.items;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;

public interface IEventTriggers {
   void onClick(GameClient var1, Room var2, Object[] var3) throws Exception;

   void onWalkOn(RoomUnit var1, Room var2, Object[] var3) throws Exception;

   void onWalkOff(RoomUnit var1, Room var2, Object[] var3) throws Exception;
}
