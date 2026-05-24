package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuardianTicket;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.BullyReportRequestComposer;
import java.util.Calendar;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/RequestReportUserBullyingEvent.class */
public class RequestReportUserBullyingEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        GuardianTicket recentTicket = Emulator.getGameEnvironment().getGuideManager().getRecentTicket(this.client.getHabbo());
        if (recentTicket != null) {
            if (recentTicket.inProgress()) {
                this.client.sendResponse(new BullyReportRequestComposer(1, 1));
                return;
            } else if ((Calendar.getInstance().getTime().getTime() / 1000) - recentTicket.getDate().getTime() < Emulator.getConfig().getInt("guardians.reporting.cooldown")) {
                this.client.sendResponse(new BullyReportRequestComposer(3, 1));
                return;
            }
        }
        this.client.sendResponse(new BullyReportRequestComposer(0, 0));
    }
}
