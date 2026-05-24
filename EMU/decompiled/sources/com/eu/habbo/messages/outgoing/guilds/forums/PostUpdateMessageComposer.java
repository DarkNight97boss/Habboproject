package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.habbohotel.guilds.forums.ForumThreadComment;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/PostUpdateMessageComposer.class */
public class PostUpdateMessageComposer extends MessageComposer {
    public final int guildId;
    public final int threadId;
    public final ForumThreadComment comment;

    public PostUpdateMessageComposer(int i, int i2, ForumThreadComment forumThreadComment) {
        this.guildId = i;
        this.threadId = i2;
        this.comment = forumThreadComment;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PostUpdateMessageComposer);
        this.response.appendInt(Integer.valueOf(this.guildId));
        this.response.appendInt(Integer.valueOf(this.threadId));
        this.comment.serialize(this.response);
        return this.response;
    }
}
