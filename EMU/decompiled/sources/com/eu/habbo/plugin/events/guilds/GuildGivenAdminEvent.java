package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildGivenAdminEvent.class */
public class GuildGivenAdminEvent extends GuildEvent {
    public final int userId;
    public final Habbo habbo;
    public final Habbo admin;

    public GuildGivenAdminEvent(Guild guild, int i, Habbo habbo, Habbo habbo2) {
        super(guild);
        this.userId = i;
        this.habbo = habbo;
        this.admin = habbo2;
    }
}
