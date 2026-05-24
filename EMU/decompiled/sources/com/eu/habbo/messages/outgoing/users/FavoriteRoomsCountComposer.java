package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.procedure.TIntProcedure;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/FavoriteRoomsCountComposer.class */
public class FavoriteRoomsCountComposer extends MessageComposer {
    private final Habbo habbo;

    public FavoriteRoomsCountComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FavoriteRoomsCountComposer);
        this.response.appendInt(Integer.valueOf(Emulator.getConfig().getInt("hotel.rooms.max.favorite")));
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().getFavoriteRooms().size()));
        this.habbo.getHabboStats().getFavoriteRooms().forEach(new TIntProcedure() { // from class: com.eu.habbo.messages.outgoing.users.FavoriteRoomsCountComposer.1
            public boolean execute(int i) {
                FavoriteRoomsCountComposer.this.response.appendInt(Integer.valueOf(i));
                return true;
            }
        });
        return this.response;
    }
}
