package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.habbohotel.guilds.forums.ForumThreadComment;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Collection;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/GuildForumCommentsComposer.class */
public class GuildForumCommentsComposer extends MessageComposer {
    private final int guildId;
    private final int threadId;
    private final int index;
    private final Collection<ForumThreadComment> guildForumCommentList;

    public GuildForumCommentsComposer(int i, int i2, int i3, Collection<ForumThreadComment> collection) {
        this.guildId = i;
        this.threadId = i2;
        this.index = i3;
        this.guildForumCommentList = collection;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildForumCommentsComposer);
        this.response.appendInt(Integer.valueOf(this.guildId));
        this.response.appendInt(Integer.valueOf(this.threadId));
        this.response.appendInt(Integer.valueOf(this.index));
        this.response.appendInt(Integer.valueOf(this.guildForumCommentList.size()));
        Iterator<ForumThreadComment> it = this.guildForumCommentList.iterator();
        while (it.hasNext()) {
            it.next().serialize(this.response);
        }
        return this.response;
    }
}
