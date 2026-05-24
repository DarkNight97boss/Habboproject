package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserTagsComposer.class */
public class RoomUserTagsComposer extends MessageComposer {
    private final Habbo habbo;

    public RoomUserTagsComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserTagsComposer);
        this.response.appendInt(Integer.valueOf(this.habbo.getRoomUnit().getId()));
        this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().tags.length));
        for (String str : this.habbo.getHabboStats().tags) {
            this.response.appendString(str);
        }
        return this.response;
    }
}
