package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.UserHomeRoomComposer;

public class SetHomeRoomEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        int roomId = this.packet.readInt();

        if (roomId != 0) {
            com.eu.habbo.habbohotel.rooms.Room room = com.eu.habbo.Emulator.getGameEnvironment().getRoomManager().getRoom(roomId);
            if (room != null && room.getOwnerId() != this.client.getHabbo().getHabboInfo().getId()
                    && !this.client.getHabbo().hasPermission(com.eu.habbo.habbohotel.permissions.Permission.ACC_ANYROOMOWNER)) {
                return; // can't set another user's room as your home room
            }
        }

        if (roomId != this.client.getHabbo().getHabboInfo().getHomeRoom()) {
            this.client.getHabbo().getHabboInfo().setHomeRoom(roomId);
            this.client.sendResponse(new UserHomeRoomComposer(this.client.getHabbo().getHabboInfo().getHomeRoom(), 0));
        }
    }
}
