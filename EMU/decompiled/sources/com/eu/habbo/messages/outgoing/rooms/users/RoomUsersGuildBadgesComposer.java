package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectObjectProcedure;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUsersGuildBadgesComposer.class */
public class RoomUsersGuildBadgesComposer extends MessageComposer {
    private final THashMap<Integer, String> guildBadges;

    public RoomUsersGuildBadgesComposer(THashMap<Integer, String> tHashMap) {
        this.guildBadges = tHashMap;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUsersGuildBadgesComposer);
        this.response.appendInt(Integer.valueOf(this.guildBadges.size()));
        this.guildBadges.forEachEntry(new TObjectObjectProcedure<Integer, String>() { // from class: com.eu.habbo.messages.outgoing.rooms.users.RoomUsersGuildBadgesComposer.1
            public boolean execute(Integer num, String str) {
                RoomUsersGuildBadgesComposer.this.response.appendInt(num);
                RoomUsersGuildBadgesComposer.this.response.appendString(str);
                return true;
            }
        });
        return this.response;
    }
}
