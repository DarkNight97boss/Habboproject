package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserRespectedEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserGiveRespectEvent.class */
public class RoomUserGiveRespectEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboStats().respectPointsToGive > 0) {
            Habbo habbo = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(iIntValue);
            if (Emulator.getPluginManager().isRegistered(UserRespectedEvent.class, false) && ((UserRespectedEvent) Emulator.getPluginManager().fireEvent(new UserRespectedEvent(habbo, this.client.getHabbo()))).isCancelled()) {
                return;
            }
            this.client.getHabbo().respect(habbo);
        }
    }
}
