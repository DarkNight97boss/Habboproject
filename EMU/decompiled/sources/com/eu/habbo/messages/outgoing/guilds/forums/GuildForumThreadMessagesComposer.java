package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/GuildForumThreadMessagesComposer.class */
public class GuildForumThreadMessagesComposer extends MessageComposer {
    public final ForumThread thread;

    public GuildForumThreadMessagesComposer(ForumThread forumThread) {
        this.thread = forumThread;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildForumThreadMessagesComposer);
        this.response.appendInt(Integer.valueOf(this.thread.getGuildId()));
        this.thread.serialize(this.response);
        return this.response;
    }
}
