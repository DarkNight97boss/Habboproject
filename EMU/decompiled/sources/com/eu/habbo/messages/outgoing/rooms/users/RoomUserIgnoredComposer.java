package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserIgnoredComposer.class */
public class RoomUserIgnoredComposer extends MessageComposer {
    public static final int IGNORED = 1;
    public static final int MUTED = 2;
    public static final int UNIGNORED = 3;
    private final Habbo habbo;
    private final int state;

    public RoomUserIgnoredComposer(Habbo habbo, int i) {
        this.habbo = habbo;
        this.state = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserIgnoredComposer);
        this.response.appendInt(Integer.valueOf(this.state));
        this.response.appendString(this.habbo.getHabboInfo().getUsername());
        return this.response;
    }
}
