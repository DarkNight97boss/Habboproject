package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.forums.ForumThread;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolReportReceivedAlertComposer;
import com.eu.habbo.threading.runnables.InsertModToolIssue;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ReportCommentEvent.class */
public class ReportCommentEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        ForumThread byId;
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        int iIntValue4 = this.packet.readInt().intValue();
        String string = this.packet.readString();
        if (Emulator.getGameEnvironment().getModToolManager().getCfhTopic(iIntValue4) == null || (byId = ForumThread.getById(iIntValue2)) == null) {
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(byId.getOpenerId());
        ModToolIssue modToolIssue = new ModToolIssue(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getUsername(), byId.getOpenerId(), habbo == null ? Emulator.PREVIEW : habbo.getHabboInfo().getUsername(), 0, string, ModToolTicketType.DISCUSSION);
        modToolIssue.category = iIntValue4;
        modToolIssue.groupId = iIntValue;
        modToolIssue.threadId = iIntValue2;
        modToolIssue.commentId = iIntValue3;
        new InsertModToolIssue(modToolIssue).run();
        this.client.sendResponse(new ModToolReportReceivedAlertComposer(0, string));
        Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue);
        Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue);
    }
}
