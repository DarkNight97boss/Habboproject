package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.habbohotel.guilds.forums.ForumThreadComment;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/GuildForumAddCommentComposer.class */
public class GuildForumAddCommentComposer extends MessageComposer {
    private final ForumThreadComment comment;

    public GuildForumAddCommentComposer(ForumThreadComment forumThreadComment) {
        this.comment = forumThreadComment;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildForumAddCommentComposer);
        this.response.appendInt(Integer.valueOf(this.comment.getThread().getGuildId()));
        this.response.appendInt(Integer.valueOf(this.comment.getThreadId()));
        this.comment.serialize(this.response);
        return this.response;
    }
}
