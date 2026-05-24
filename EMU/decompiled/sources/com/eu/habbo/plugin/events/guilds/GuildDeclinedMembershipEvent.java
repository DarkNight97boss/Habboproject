package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildDeclinedMembershipEvent.class */
public class GuildDeclinedMembershipEvent extends GuildEvent {
    public final int userId;
    public final Habbo user;
    public final Habbo admin;

    public GuildDeclinedMembershipEvent(Guild guild, int i, Habbo habbo, Habbo habbo2) {
        super(guild);
        this.userId = i;
        this.user = habbo;
        this.admin = habbo2;
    }
}
