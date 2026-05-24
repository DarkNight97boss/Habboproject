package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guilds.GuildFurniWidgetComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/RequestGuildFurniWidgetEvent.class */
public class RequestGuildFurniWidgetEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue);
            Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue2);
            if (habboItem == null || guild == null) {
                return;
            }
            this.client.sendResponse(new GuildFurniWidgetComposer(this.client.getHabbo(), guild, habboItem));
        }
    }
}
