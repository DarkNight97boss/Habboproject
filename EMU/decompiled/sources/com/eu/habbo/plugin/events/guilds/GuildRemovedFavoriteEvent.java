package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildRemovedFavoriteEvent.class */
public class GuildRemovedFavoriteEvent extends GuildEvent {
    public final Habbo habbo;

    public GuildRemovedFavoriteEvent(Guild guild, Habbo habbo) {
        super(guild);
        this.habbo = habbo;
    }
}
