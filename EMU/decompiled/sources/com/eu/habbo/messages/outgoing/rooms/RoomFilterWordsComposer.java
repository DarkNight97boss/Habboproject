package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomFilterWordsComposer.class */
public class RoomFilterWordsComposer extends MessageComposer {
    private final Room room;

    public RoomFilterWordsComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomFilterWordsComposer);
        this.response.appendInt(Integer.valueOf(this.room.getWordFilterWords().size()));
        TObjectHashIterator it = this.room.getWordFilterWords().iterator();
        while (it.hasNext()) {
            this.response.appendString((String) it.next());
        }
        return this.response;
    }
}
