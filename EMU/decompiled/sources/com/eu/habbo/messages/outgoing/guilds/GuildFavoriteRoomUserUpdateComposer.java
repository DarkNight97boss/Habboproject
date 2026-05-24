package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildFavoriteRoomUserUpdateComposer.class */
public class GuildFavoriteRoomUserUpdateComposer extends MessageComposer {
    private RoomUnit roomUnit;
    private Guild guild;

    public GuildFavoriteRoomUserUpdateComposer(RoomUnit roomUnit, Guild guild) {
        this.roomUnit = roomUnit;
        this.guild = guild;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildFavoriteRoomUserUpdateComposer);
        this.response.appendInt(Integer.valueOf(this.roomUnit.getId()));
        this.response.appendInt(Integer.valueOf(this.guild != null ? this.guild.getId() : 0));
        this.response.appendInt(Integer.valueOf(this.guild != null ? this.guild.getState().state : 3));
        this.response.appendString(this.guild != null ? this.guild.getName() : Emulator.PREVIEW);
        return this.response;
    }
}
