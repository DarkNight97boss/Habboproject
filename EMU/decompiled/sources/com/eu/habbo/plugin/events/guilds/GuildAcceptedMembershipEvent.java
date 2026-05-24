package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildAcceptedMembershipEvent.class */
public class GuildAcceptedMembershipEvent extends GuildEvent {
    public final int userId;
    public final Habbo user;

    public GuildAcceptedMembershipEvent(Guild guild, int i, Habbo habbo) {
        super(guild);
        this.userId = i;
        this.user = habbo;
    }
}
