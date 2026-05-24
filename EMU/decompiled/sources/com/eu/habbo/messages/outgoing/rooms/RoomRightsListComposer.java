package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.hash.THashMap;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomRightsListComposer.class */
public class RoomRightsListComposer extends MessageComposer {
    private final Room room;

    public RoomRightsListComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomRightsListComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        THashMap<Integer, String> usersWithRights = this.room.getUsersWithRights();
        this.response.appendInt(Integer.valueOf(usersWithRights.size()));
        for (Map.Entry entry : usersWithRights.entrySet()) {
            this.response.appendInt((Integer) entry.getKey());
            this.response.appendString((String) entry.getValue());
        }
        return this.response;
    }
}
