package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ReportRoomFormComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/RequestReportRoomEvent.class */
public class RequestReportRoomEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        this.client.sendResponse(new ReportRoomFormComposer(Emulator.getGameEnvironment().getModToolManager().openTicketsForHabbo(this.client.getHabbo())));
    }
}
