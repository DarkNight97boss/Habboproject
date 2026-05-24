package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildEditFailComposer.class */
public class GuildEditFailComposer extends MessageComposer {
    public static final int ROOM_ALREADY_IN_USE = 0;
    public static final int INVALID_GUILD_NAME = 1;
    public static final int HC_REQUIRED = 2;
    public static final int MAX_GUILDS_JOINED = 3;
    private int errorCode;

    public GuildEditFailComposer(int i) {
        this.errorCode = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildEditFailComposer);
        this.response.appendInt(Integer.valueOf(this.errorCode));
        return this.response;
    }
}
