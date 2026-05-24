package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomBan;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.NoSuchElementException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomBannedUsersComposer.class */
public class RoomBannedUsersComposer extends MessageComposer {
    private final Room room;

    public RoomBannedUsersComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        THashSet tHashSet = new THashSet();
        TIntObjectIterator it = this.room.getBannedHabbos().iterator();
        int size = this.room.getBannedHabbos().size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                if (((RoomBan) it.value()).endTimestamp > intUnixTimestamp) {
                    tHashSet.add((RoomBan) it.value());
                }
            } catch (NoSuchElementException e) {
            }
        }
        if (tHashSet.isEmpty()) {
            return null;
        }
        this.response.init(Outgoing.RoomBannedUsersComposer);
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendInt(Integer.valueOf(tHashSet.size()));
        TObjectHashIterator it2 = tHashSet.iterator();
        while (it2.hasNext()) {
            RoomBan roomBan = (RoomBan) it2.next();
            this.response.appendInt(Integer.valueOf(roomBan.userId));
            this.response.appendString(roomBan.username);
        }
        return this.response;
    }
}
