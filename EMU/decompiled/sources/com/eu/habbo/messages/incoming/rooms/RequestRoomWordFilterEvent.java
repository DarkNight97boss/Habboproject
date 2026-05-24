package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomFilterWordsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RequestRoomWordFilterEvent.class */
public class RequestRoomWordFilterEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.packet.readInt().intValue());
        if (room == null || !room.hasRights(this.client.getHabbo())) {
            return;
        }
        this.client.sendResponse(new RoomFilterWordsComposer(room));
        AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("SelfModRoomFilterSeen"));
    }
}
