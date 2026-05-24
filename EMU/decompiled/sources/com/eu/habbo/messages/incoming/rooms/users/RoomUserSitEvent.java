package com.eu.habbo.messages.incoming.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserIdleEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/users/RoomUserSitEvent.class */
public class RoomUserSitEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            if (this.client.getHabbo().getRoomUnit().isWalking()) {
                this.client.getHabbo().getRoomUnit().stopWalking();
            }
            this.client.getHabbo().getHabboInfo().getCurrentRoom().makeSit(this.client.getHabbo());
            UserIdleEvent userIdleEvent = new UserIdleEvent(this.client.getHabbo(), UserIdleEvent.IdleReason.WALKED, false);
            Emulator.getPluginManager().fireEvent(userIdleEvent);
            if (userIdleEvent.isCancelled() || userIdleEvent.idle) {
                return;
            }
            this.client.getHabbo().getHabboInfo().getCurrentRoom().unIdle(this.client.getHabbo());
        }
    }
}
