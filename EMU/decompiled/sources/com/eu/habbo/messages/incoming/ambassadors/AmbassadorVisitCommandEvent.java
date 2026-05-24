package com.eu.habbo.messages.incoming.ambassadors;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/ambassadors/AmbassadorVisitCommandEvent.class */
public class AmbassadorVisitCommandEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().hasPermission(Permission.ACC_AMBASSADOR)) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(this.packet.readString());
            if (habbo == null || habbo.getHabboInfo().getCurrentRoom() == null) {
                return;
            }
            this.client.sendResponse(new ForwardToRoomComposer(habbo.getHabboInfo().getCurrentRoom().getId()));
        }
    }
}
