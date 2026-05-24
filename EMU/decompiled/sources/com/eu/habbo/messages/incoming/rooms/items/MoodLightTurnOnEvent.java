package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionMoodLight;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomMoodlightData;
import com.eu.habbo.habbohotel.rooms.RoomRightLevels;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/MoodLightTurnOnEvent.class */
public class MoodLightTurnOnEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom.getGuildId() <= 0 || !currentRoom.getGuildRightLevel(this.client.getHabbo()).isLessThan(RoomRightLevels.GUILD_RIGHTS) || currentRoom.hasRights(this.client.getHabbo())) {
            TObjectHashIterator it = currentRoom.getRoomSpecialTypes().getItemsOfType(InteractionMoodLight.class).iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                String string = "2,1,2,#FF00FF,255";
                Iterator it2 = currentRoom.getMoodlightData().valueCollection().iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    RoomMoodlightData roomMoodlightData = (RoomMoodlightData) it2.next();
                    if (roomMoodlightData.isEnabled()) {
                        string = roomMoodlightData.toString();
                        break;
                    }
                }
                RoomMoodlightData roomMoodlightDataFromString = RoomMoodlightData.fromString(string);
                if (RoomMoodlightData.fromString(habboItem.getExtradata()).isEnabled()) {
                    roomMoodlightDataFromString.disable();
                }
                habboItem.setExtradata(roomMoodlightDataFromString.toString());
                habboItem.needsUpdate(true);
                currentRoom.updateItem(habboItem);
                Emulator.getThreading().run(habboItem);
            }
        }
    }
}
