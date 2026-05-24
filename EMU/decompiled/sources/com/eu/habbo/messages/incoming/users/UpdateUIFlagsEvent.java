package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/UpdateUIFlagsEvent.class */
public class UpdateUIFlagsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.getHabbo().getHabboStats().uiFlags = this.packet.readInt().intValue();
        this.client.getHabbo().getHabboStats().run();
    }
}
