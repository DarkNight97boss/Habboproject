package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.UserProfileComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/RequestUserProfileEvent.class */
public class RequestUserProfileEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue);
        if (habbo != null) {
            this.client.sendResponse(new UserProfileComposer(habbo, this.client));
        } else {
            this.client.sendResponse(new UserProfileComposer(HabboManager.getOfflineHabboInfo(iIntValue), this.client));
        }
    }
}
