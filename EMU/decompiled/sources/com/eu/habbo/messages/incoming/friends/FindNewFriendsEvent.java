package com.eu.habbo.messages.incoming.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.friends.FriendFindingRoomComposer;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/friends/FindNewFriendsEvent.class */
public class FindNewFriendsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        List<RoomCategory> listRoomCategoriesForHabbo = Emulator.getGameEnvironment().getRoomManager().roomCategoriesForHabbo(this.client.getHabbo());
        Collections.shuffle(listRoomCategoriesForHabbo);
        Iterator<RoomCategory> it = listRoomCategoriesForHabbo.iterator();
        while (it.hasNext()) {
            List<Room> activeRooms = Emulator.getGameEnvironment().getRoomManager().getActiveRooms(it.next().getId());
            if (!activeRooms.isEmpty()) {
                Room room = activeRooms.get(0);
                if (room.getUserCount() > 0) {
                    this.client.sendResponse(new ForwardToRoomComposer(room.getId()));
                    return;
                }
            }
        }
        this.client.sendResponse(new FriendFindingRoomComposer(0));
    }
}
