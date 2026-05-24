package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserNameChangedComposer.class */
public class RoomUserNameChangedComposer extends MessageComposer {
    private final int userId;
    private final int roomId;
    private final String name;

    public RoomUserNameChangedComposer(Habbo habbo) {
        this(habbo, false);
    }

    public RoomUserNameChangedComposer(Habbo habbo, boolean z) {
        this.userId = habbo.getHabboInfo().getId();
        this.roomId = habbo.getRoomUnit().getId();
        this.name = (z ? Room.PREFIX_FORMAT.replace("%color%", habbo.getHabboInfo().getRank().getPrefixColor()).replace("%prefix%", habbo.getHabboInfo().getRank().getPrefix()) : Emulator.PREVIEW) + habbo.getHabboInfo().getUsername();
    }

    public RoomUserNameChangedComposer(int i, int i2, String str) {
        this.userId = i;
        this.roomId = i2;
        this.name = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserNameChangedComposer);
        this.response.appendInt(Integer.valueOf(this.userId));
        this.response.appendInt(Integer.valueOf(this.roomId));
        this.response.appendString(this.name);
        return this.response;
    }
}
