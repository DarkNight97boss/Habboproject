package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.hotelview.HotelViewComposer;
import com.eu.habbo.messages.outgoing.rooms.HideDoorbellComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomAccessDeniedComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/HandleDoorbellEvent.class */
public class HandleDoorbellEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() == null || !this.client.getHabbo().getHabboInfo().getCurrentRoom().hasRights(this.client.getHabbo())) {
            return;
        }
        String string = this.packet.readString();
        boolean z = this.packet.readBoolean();
        Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(string);
        if (habbo == null || habbo.getHabboInfo().getRoomQueueId() != this.client.getHabbo().getHabboInfo().getCurrentRoom().getId()) {
            return;
        }
        this.client.getHabbo().getHabboInfo().getCurrentRoom().removeFromQueue(habbo);
        if (z) {
            habbo.getClient().sendResponse(new HideDoorbellComposer(Emulator.PREVIEW));
            Emulator.getGameEnvironment().getRoomManager().enterRoom(habbo, this.client.getHabbo().getHabboInfo().getCurrentRoom().getId(), Emulator.PREVIEW, true);
        } else {
            habbo.getClient().sendResponse(new RoomAccessDeniedComposer(Emulator.PREVIEW));
            habbo.getClient().sendResponse(new HotelViewComposer());
        }
        habbo.getHabboInfo().setRoomQueueId(0);
    }
}
