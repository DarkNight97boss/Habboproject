package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.eu.habbo.messages.outgoing.users.UserHomeRoomComposer;
import java.util.ArrayList;
import java.util.Collections;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/NewNavigatorActionEvent.class */
public class NewNavigatorActionEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        if (!string.equals("random_friending_room")) {
            if (string.equalsIgnoreCase("predefined_noob_lobby")) {
                this.client.sendResponse(new ForwardToRoomComposer(Emulator.getConfig().getInt("hotel.room.nooblobby")));
                return;
            } else {
                this.client.sendResponse(new UserHomeRoomComposer(this.client.getHabbo().getHabboInfo().getHomeRoom(), this.client.getHabbo().getHabboInfo().getHomeRoom()));
                return;
            }
        }
        ArrayList<Room> activeRooms = Emulator.getGameEnvironment().getRoomManager().getActiveRooms();
        if (activeRooms.isEmpty()) {
            return;
        }
        Collections.shuffle(activeRooms);
        this.client.sendResponse(new ForwardToRoomComposer(activeRooms.get(0).getId()));
    }
}
