package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildBuyRoomsComposer;
import gnu.trove.set.hash.THashSet;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/RequestGuildBuyRoomsEvent.class */
public class RequestGuildBuyRoomsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        List<Room> roomsForHabbo = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(this.client.getHabbo());
        THashSet tHashSet = new THashSet();
        for (Room room : roomsForHabbo) {
            if (room.getGuildId() == 0) {
                tHashSet.add(room);
            }
        }
        this.client.sendResponse(new GuildBuyRoomsComposer(tHashSet));
    }
}
