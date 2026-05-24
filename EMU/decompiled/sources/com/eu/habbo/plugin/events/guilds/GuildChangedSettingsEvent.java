package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildChangedSettingsEvent.class */
public class GuildChangedSettingsEvent extends GuildEvent {
    public int state;
    public boolean rights;

    public GuildChangedSettingsEvent(Guild guild, int i, boolean z) {
        super(guild);
        this.state = i;
        this.rights = z;
    }
}
