package com.eu.habbo.plugin.events.guilds;

import com.eu.habbo.habbohotel.guilds.Guild;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/GuildChangedColorsEvent.class */
public class GuildChangedColorsEvent extends GuildEvent {
    public int colorOne;
    public int colorTwo;

    public GuildChangedColorsEvent(Guild guild, int i, int i2) {
        super(guild);
        this.colorOne = i;
        this.colorTwo = i2;
    }
}
