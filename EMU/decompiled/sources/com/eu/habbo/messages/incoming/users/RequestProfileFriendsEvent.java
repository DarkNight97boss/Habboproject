package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.ProfileFriendsComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/RequestProfileFriendsEvent.class */
public class RequestProfileFriendsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue);
        if (habbo != null) {
            this.client.sendResponse(new ProfileFriendsComposer(habbo));
        } else {
            this.client.sendResponse(new ProfileFriendsComposer(Messenger.getFriends(iIntValue), iIntValue));
        }
    }
}
