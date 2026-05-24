package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionMoodLight;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomMoodlightData;
import com.eu.habbo.habbohotel.rooms.RoomRightLevels;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import gnu.trove.iterator.hash.TObjectHashIterator;

public class MoodLightTurnOnEvent extends MessageHandler {
   @Override
   public void handle() throws Exception {
      Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
      if (room.getGuildId() <= 0
         || !room.getGuildRightLevel(this.client.getHabbo()).isLessThan(RoomRightLevels.GUILD_RIGHTS)
         || room.hasRights(this.client.getHabbo())) {
         TObjectHashIterator var2 = room.getRoomSpecialTypes().getItemsOfType(InteractionMoodLight.class).iterator();

         while (var2.hasNext()) {
            HabboItem moodLight = (HabboItem)var2.next();
            String extradata = "2,1,2,#FF00FF,255";

            for (RoomMoodlightData data : room.getMoodlightData().valueCollection()) {
               if (data.isEnabled()) {
                  extradata = data.toString();
                  break;
               }
            }

            RoomMoodlightData adjusted = RoomMoodlightData.fromString(extradata);
            if (RoomMoodlightData.fromString(moodLight.getExtradata()).isEnabled()) {
               adjusted.disable();
            }

            moodLight.setExtradata(adjusted.toString());
            moodLight.needsUpdate(true);
            room.updateItem(moodLight);
            Emulator.getThreading().run(moodLight);
         }
      }
   }
}
