package com.eu.habbo.messages.outgoing.rooms.promotions;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/promotions/PromoteOwnRoomsListComposer.class */
public class PromoteOwnRoomsListComposer extends MessageComposer {
    private final List<Room> rooms = new ArrayList();

    public PromoteOwnRoomsListComposer(List<Room> list) {
        for (Room room : list) {
            if (!room.isPromoted()) {
                this.rooms.add(room);
            }
        }
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PromoteOwnRoomsListComposer);
        this.response.appendBoolean(true);
        this.response.appendInt(Integer.valueOf(this.rooms.size()));
        for (Room room : this.rooms) {
            this.response.appendInt(Integer.valueOf(room.getId()));
            this.response.appendString(room.getName());
            this.response.appendBoolean(true);
        }
        return this.response;
    }
}
