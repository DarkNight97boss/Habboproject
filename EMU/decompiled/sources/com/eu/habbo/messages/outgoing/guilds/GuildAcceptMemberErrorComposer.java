package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildAcceptMemberErrorComposer.class */
public class GuildAcceptMemberErrorComposer extends MessageComposer {
    public static final int NO_LONGER_MEMBER = 0;
    public static final int ALREADY_REJECTED = 1;
    public static final int ALREADY_ACCEPTED = 2;
    private final int guildId;
    private final int errorCode;

    public GuildAcceptMemberErrorComposer(int i, int i2) {
        this.guildId = i;
        this.errorCode = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildAcceptMemberErrorComposer);
        this.response.appendInt(Integer.valueOf(this.guildId));
        this.response.appendInt(Integer.valueOf(this.errorCode));
        return this.response;
    }
}
