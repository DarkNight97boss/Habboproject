package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.Map;
import java.util.NoSuchElementException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/RoomWallItemsComposer.class */
public class RoomWallItemsComposer extends MessageComposer {
    private final Room room;

    public RoomWallItemsComposer(Room room) {
        this.room = room;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomWallItemsComposer);
        THashMap tHashMap = new THashMap();
        TIntObjectMap<String> furniOwnerNames = this.room.getFurniOwnerNames();
        TIntObjectIterator it = furniOwnerNames.iterator();
        int size = furniOwnerNames.size();
        while (true) {
            int i = size;
            size--;
            if (i <= 0) {
                break;
            }
            try {
                it.advance();
                tHashMap.put(Integer.valueOf(it.key()), (String) it.value());
            } catch (NoSuchElementException e) {
            }
        }
        this.response.appendInt(Integer.valueOf(tHashMap.size()));
        for (Map.Entry entry : tHashMap.entrySet()) {
            this.response.appendInt((Integer) entry.getKey());
            this.response.appendString((String) entry.getValue());
        }
        THashSet<HabboItem> wallItems = this.room.getWallItems();
        this.response.appendInt(Integer.valueOf(wallItems.size()));
        TObjectHashIterator it2 = wallItems.iterator();
        while (it2.hasNext()) {
            ((HabboItem) it2.next()).serializeWallData(this.response);
        }
        return this.response;
    }
}
