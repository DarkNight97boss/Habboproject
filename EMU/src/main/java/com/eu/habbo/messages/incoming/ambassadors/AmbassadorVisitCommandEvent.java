package com.eu.habbo.messages.incoming.ambassadors;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;

public class AmbassadorVisitCommandEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        if (this.client.getHabbo().hasPermission(Permission.ACC_AMBASSADOR)) {
            // Habbo usernames are <= 30 chars (CMS-side constraint). Cap defensively
            // so a malformed packet can't force a multi-KB allocation here.
            String username = this.packet.readString(64);

            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(username);

            if (habbo != null) {
                if (habbo.getHabboInfo().getCurrentRoom() != null) {
                    this.client.sendResponse(new ForwardToRoomComposer(habbo.getHabboInfo().getCurrentRoom().getId()));
                }
            }
        }
    }
}
