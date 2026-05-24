package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildRemovedAdminEvent.class */
public class GuildRemovedAdminEvent extends GuildEvent {
    public final int userId;
    public final Habbo admin;

    public GuildRemovedAdminEvent(Guild guild, int i, Habbo habbo) {
        super(guild);
        this.userId = i;
        this.admin = habbo;
    }
}
