package com.eu.habbo.messages.incoming.rooms.promotions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.promotions.PromoteOwnRoomsListComposer;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/promotions/RequestPromotionRoomsEvent.class */
public class RequestPromotionRoomsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        List<Room> roomsForHabbo = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(this.client.getHabbo());
        roomsForHabbo.addAll(Emulator.getGameEnvironment().getRoomManager().getRoomsWithRights(this.client.getHabbo()));
        roomsForHabbo.addAll(Emulator.getGameEnvironment().getRoomManager().getRoomsWithAdminRights(this.client.getHabbo()));
        this.client.sendResponse(new PromoteOwnRoomsListComposer(roomsForHabbo));
    }
}
