package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildBuyRoomsComposer.class */
public class GuildBuyRoomsComposer extends MessageComposer {
    private final THashSet<Room> rooms;

    public GuildBuyRoomsComposer(THashSet<Room> tHashSet) {
        this.rooms = tHashSet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildBuyRoomsComposer);
        this.response.appendInt(Integer.valueOf(Emulator.getConfig().getInt("catalog.guild.price")));
        this.response.appendInt(Integer.valueOf(this.rooms.size()));
        TObjectHashIterator it = this.rooms.iterator();
        while (it.hasNext()) {
            Room room = (Room) it.next();
            this.response.appendInt(Integer.valueOf(room.getId()));
            this.response.appendString(room.getName());
            this.response.appendBoolean(false);
        }
        this.response.appendInt((Integer) 5);
        this.response.appendInt((Integer) 10);
        this.response.appendInt((Integer) 3);
        this.response.appendInt((Integer) 4);
        this.response.appendInt((Integer) 25);
        this.response.appendInt((Integer) 17);
        this.response.appendInt((Integer) 5);
        this.response.appendInt((Integer) 25);
        this.response.appendInt((Integer) 17);
        this.response.appendInt((Integer) 3);
        this.response.appendInt((Integer) 29);
        this.response.appendInt((Integer) 11);
        this.response.appendInt((Integer) 4);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 0);
        return this.response;
    }
}
