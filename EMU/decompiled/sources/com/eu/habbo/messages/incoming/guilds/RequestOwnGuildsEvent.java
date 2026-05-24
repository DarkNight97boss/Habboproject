package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildListComposer;
import gnu.trove.set.hash.THashSet;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/RequestOwnGuildsEvent.class */
public class RequestOwnGuildsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Guild guild;
        THashSet tHashSet = new THashSet();
        Iterator<Integer> it = this.client.getHabbo().getHabboStats().guilds.iterator();
        while (it.hasNext()) {
            int iIntValue = it.next().intValue();
            if (iIntValue != 0 && (guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue)) != null) {
                tHashSet.add(guild);
            }
        }
        this.client.sendResponse(new GuildListComposer(tHashSet, this.client.getHabbo()));
    }
}
