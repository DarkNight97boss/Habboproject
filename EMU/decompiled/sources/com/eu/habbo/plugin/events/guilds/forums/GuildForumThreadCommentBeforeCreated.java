package com.eu.habbo.plugin.events.guilds.forums;

import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/forums/GuildForumThreadCommentBeforeCreated.class */
public class GuildForumThreadCommentBeforeCreated extends Event {
    public final ForumThread thread;
    public final Habbo poster;
    public final String message;

    public GuildForumThreadCommentBeforeCreated(ForumThread forumThread, Habbo habbo, String str) {
        this.thread = forumThread;
        this.poster = habbo;
        this.message = str;
    }
}
