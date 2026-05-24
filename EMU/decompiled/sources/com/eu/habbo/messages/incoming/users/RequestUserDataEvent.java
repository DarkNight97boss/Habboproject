package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.MeMenuSettingsComposer;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.eu.habbo.messages.outgoing.users.UserPerksComposer;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/RequestUserDataEvent.class */
public class RequestUserDataEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUserDataEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo() == null) {
            LOGGER.debug("Attempted to request user data where Habbo was null.");
            Emulator.getGameServer().getGameClientManager().disposeClient(this.client);
            return;
        }
        ArrayList<ServerMessage> arrayList = new ArrayList<>();
        arrayList.add(new UserDataComposer(this.client.getHabbo()).compose());
        arrayList.add(new UserPerksComposer(this.client.getHabbo()).compose());
        arrayList.add(new MeMenuSettingsComposer(this.client.getHabbo()).compose());
        this.client.sendResponses(arrayList);
    }
}
