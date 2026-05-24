package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatType;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserTalkEvent.class */
public class UserTalkEvent extends UserEvent {
    public final RoomChatMessage chatMessage;
    public final RoomChatType chatType;

    public UserTalkEvent(Habbo habbo, RoomChatMessage roomChatMessage, RoomChatType roomChatType) {
        super(habbo);
        this.chatMessage = roomChatMessage;
        this.chatType = roomChatType;
    }
}
