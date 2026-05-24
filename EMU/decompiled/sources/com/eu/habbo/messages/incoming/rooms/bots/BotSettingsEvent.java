package com.eu.habbo.messages.incoming.rooms.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.BotSettingsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/bots/BotSettingsEvent.class */
public class BotSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Bot bot;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        if ((currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) && (bot = currentRoom.getBot(Math.abs(this.packet.readInt().intValue()))) != null) {
            this.client.sendResponse(new BotSettingsComposer(bot, this.packet.readInt().intValue()));
        }
    }
}
