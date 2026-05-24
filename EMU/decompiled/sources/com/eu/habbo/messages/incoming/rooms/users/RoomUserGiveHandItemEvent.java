package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.threading.runnables.HabboGiveHandItemToHabbo;
import com.eu.habbo.threading.runnables.RoomUnitWalkToRoomUnit;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserGiveHandItemEvent.class */
public class RoomUserGiveHandItemEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Habbo habbo;
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (habbo = currentRoom.getHabbo(iIntValue)) == null) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(new HabboGiveHandItemToHabbo(this.client.getHabbo(), habbo));
        Emulator.getThreading().run(new RoomUnitWalkToRoomUnit(this.client.getHabbo().getRoomUnit(), habbo.getRoomUnit(), this.client.getHabbo().getHabboInfo().getCurrentRoom(), arrayList, arrayList));
    }
}
