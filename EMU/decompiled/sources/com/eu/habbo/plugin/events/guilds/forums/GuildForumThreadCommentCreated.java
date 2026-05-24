package com.eu.habbo.plugin.events.guilds.forums;

import com.eu.habbo.habbohotel.guilds.forums.ForumThreadComment;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/guilds/forums/GuildForumThreadCommentCreated.class */
public class GuildForumThreadCommentCreated extends Event {
    public final ForumThreadComment comment;

    public GuildForumThreadCommentCreated(ForumThreadComment forumThreadComment) {
        this.comment = forumThreadComment;
    }
}
