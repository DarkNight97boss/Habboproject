package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/ThreadUpdatedMessageComposer.class */
public class ThreadUpdatedMessageComposer extends MessageComposer {
    public final Guild guild;
    public final ForumThread thread;
    private final Habbo habbo;
    private final boolean isPinned;
    private final boolean isLocked;

    public ThreadUpdatedMessageComposer(Guild guild, ForumThread forumThread, Habbo habbo, boolean z, boolean z2) {
        this.guild = guild;
        this.habbo = habbo;
        this.thread = forumThread;
        this.isPinned = z;
        this.isLocked = z2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ThreadUpdateMessageComposer);
        this.response.appendInt(Integer.valueOf(this.thread.getGuildId()));
        this.thread.serialize(this.response);
        return this.response;
    }
}
