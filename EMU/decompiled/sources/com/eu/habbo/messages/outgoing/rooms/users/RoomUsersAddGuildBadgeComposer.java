package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUsersAddGuildBadgeComposer.class */
public class RoomUsersAddGuildBadgeComposer extends MessageComposer {
    private final Guild guild;

    public RoomUsersAddGuildBadgeComposer(Guild guild) {
        this.guild = guild;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUsersGuildBadgesComposer);
        this.response.appendInt((Integer) 1);
        this.response.appendInt(Integer.valueOf(this.guild.getId()));
        this.response.appendString(this.guild.getBadge());
        return this.response;
    }
}
