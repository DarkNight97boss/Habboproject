package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.guides.GuideSessionDetachedComposer;
import com.eu.habbo.messages.outgoing.guides.GuideSessionEndedComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolReportReceivedAlertComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guides/GuideReportHelperEvent.class */
public class GuideReportHelperEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        GuideTour guideTourByHabbo = Emulator.getGameEnvironment().getGuideManager().getGuideTourByHabbo(this.client.getHabbo());
        if (guideTourByHabbo != null) {
            Habbo helper = guideTourByHabbo.getHelper();
            if (helper == this.client.getHabbo()) {
                helper = guideTourByHabbo.getNoob();
            }
            ModToolIssue modToolIssue = new ModToolIssue(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getUsername(), helper.getHabboInfo().getId(), helper.getHabboInfo().getUsername(), 0, string, ModToolTicketType.GUIDE_SYSTEM);
            Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue);
            Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue);
            this.client.sendResponse(new ModToolReportReceivedAlertComposer(0, string));
            this.client.sendResponse(new GuideSessionDetachedComposer());
            this.client.sendResponse(new GuideSessionEndedComposer(1));
            helper.getClient().sendResponse(new GuideSessionDetachedComposer());
            helper.getClient().sendResponse(new GuideSessionEndedComposer(1));
        }
    }
}
