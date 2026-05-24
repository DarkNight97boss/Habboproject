package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.navigator.RoomCategoriesComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/RequestRoomCategoriesEvent.class */
public class RequestRoomCategoriesEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new RoomCategoriesComposer(Emulator.getGameEnvironment().getRoomManager().roomCategoriesForHabbo(this.client.getHabbo())));
    }
}
