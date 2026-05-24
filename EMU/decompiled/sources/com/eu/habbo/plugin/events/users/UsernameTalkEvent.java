package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UsernameTalkEvent.class */
public class UsernameTalkEvent extends UserEvent {
    public final RoomChatMessage chatMessage;
    public final RoomChatType chatType;
    private ServerMessage customComposer;

    public UsernameTalkEvent(Habbo habbo, RoomChatMessage roomChatMessage, RoomChatType roomChatType) {
        super(habbo);
        this.customComposer = null;
        this.chatMessage = roomChatMessage;
        this.chatType = roomChatType;
    }

    public void setCustomComposer(ServerMessage serverMessage) {
        this.customComposer = serverMessage;
    }

    public boolean hasCustomComposer() {
        return this.customComposer != null;
    }

    public ServerMessage getCustomComposer() {
        return this.customComposer;
    }
}
