package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildChangedBadgeEvent.class */
public class GuildChangedBadgeEvent extends GuildEvent {
    public String badge;

    public GuildChangedBadgeEvent(Guild guild, String str) {
        super(guild);
        this.badge = str;
    }
}
