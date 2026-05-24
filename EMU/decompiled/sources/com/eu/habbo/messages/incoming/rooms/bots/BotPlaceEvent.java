package com.eu.habbo.messages.incoming.rooms.bots;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/bots/BotPlaceEvent.class */
public class BotPlaceEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Bot bot;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || (bot = this.client.getHabbo().getInventory().getBotsComponent().getBot(this.packet.readInt().intValue())) == null) {
            return;
        }
        Emulator.getGameEnvironment().getBotManager().placeBot(bot, this.client.getHabbo(), this.client.getHabbo().getHabboInfo().getCurrentRoom(), currentRoom.getLayout().getTile((short) this.packet.readInt().intValue(), (short) this.packet.readInt().intValue()));
    }
}
