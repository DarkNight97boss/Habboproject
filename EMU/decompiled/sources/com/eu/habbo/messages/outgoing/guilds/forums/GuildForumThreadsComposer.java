package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/GuildForumThreadsComposer.class */
public class GuildForumThreadsComposer extends MessageComposer {
    public final Guild guild;
    public final int index;

    public GuildForumThreadsComposer(Guild guild, int i) {
        this.guild = guild;
        this.index = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        try {
            ArrayList arrayList = new ArrayList((Collection) ForumThread.getByGuildId(this.guild.getId()));
            arrayList.sort(Comparator.comparingInt(forumThread -> {
                if (forumThread.isPinned()) {
                    return Integer.MAX_VALUE;
                }
                return forumThread.getUpdatedAt();
            }));
            Collections.reverse(arrayList);
            Iterator it = arrayList.iterator();
            int size = arrayList.size() > 20 ? 20 : arrayList.size();
            this.response.init(Outgoing.GuildForumThreadsComposer);
            this.response.appendInt(Integer.valueOf(this.guild.getId()));
            this.response.appendInt(Integer.valueOf(this.index));
            this.response.appendInt(Integer.valueOf(size));
            for (int i = 0; i < this.index && it.hasNext(); i++) {
                it.next();
            }
            for (int i2 = 0; i2 < size && it.hasNext(); i2++) {
                ((ForumThread) it.next()).serialize(this.response);
            }
            return this.response;
        } catch (Exception e) {
            return new ConnectionErrorComposer(500).compose();
        }
    }
}
