package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildChangedNameEvent.class */
public class GuildChangedNameEvent extends GuildEvent {
    public String name;
    public String description;

    public GuildChangedNameEvent(Guild guild, String str, String str2) {
        super(guild);
        this.name = str;
        this.description = str2;
    }
}
