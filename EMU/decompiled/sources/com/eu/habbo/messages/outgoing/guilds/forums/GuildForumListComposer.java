package com.eu.habbo.messages.outgoing.guilds.forums;

import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.Iterator;
import java.util.Set;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/forums/GuildForumListComposer.class */
public class GuildForumListComposer extends MessageComposer {
    private final Set<Guild> guilds;
    private final Habbo habbo;
    private final int mode;
    private final int index;

    public GuildForumListComposer(Set<Guild> set, Habbo habbo, int i, int i2) {
        this.guilds = set;
        this.habbo = habbo;
        this.mode = i;
        this.index = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3001);
        this.response.appendInt(Integer.valueOf(this.mode));
        this.response.appendInt(Integer.valueOf(this.guilds.size()));
        this.response.appendInt(Integer.valueOf(this.index));
        Iterator<Guild> it = this.guilds.iterator();
        int size = this.guilds.size() > 20 ? 20 : this.guilds.size();
        this.response.appendInt(Integer.valueOf(size));
        for (int i = 0; i < this.index && it.hasNext(); i++) {
            it.next();
        }
        for (int i2 = 0; i2 < size && it.hasNext(); i2++) {
            GuildForumDataComposer.serializeForumData(this.response, it.next(), this.habbo);
        }
        return this.response;
    }
}
