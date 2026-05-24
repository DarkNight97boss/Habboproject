package com.eu.habbo.messages.incoming.gamecenter;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.gamecenter.GameCenterAccountInfoComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/gamecenter/GameCenterRequestAccountStatusEvent.class */
public class GameCenterRequestAccountStatusEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new GameCenterAccountInfoComposer(this.packet.readInt().intValue(), 10));
    }
}
