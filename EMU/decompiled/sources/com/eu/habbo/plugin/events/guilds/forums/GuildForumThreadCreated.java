package com.eu.habbo.plugin.events.guilds.forums;

import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/forums/GuildForumThreadCreated.class */
public class GuildForumThreadCreated extends Event {
    public final ForumThread thread;

    public GuildForumThreadCreated(ForumThread forumThread) {
        this.thread = forumThread;
    }
}
