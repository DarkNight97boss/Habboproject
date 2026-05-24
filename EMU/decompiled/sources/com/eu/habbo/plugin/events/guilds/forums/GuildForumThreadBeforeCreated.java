package com.eu.habbo.plugin.events.guilds.forums;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/forums/GuildForumThreadBeforeCreated.class */
public class GuildForumThreadBeforeCreated extends Event {
    public final Guild guild;
    public final Habbo opener;
    public final String subject;
    public final String message;

    public GuildForumThreadBeforeCreated(Guild guild, Habbo habbo, String str, String str2) {
        this.guild = guild;
        this.opener = habbo;
        this.subject = str;
        this.message = str2;
    }
}
