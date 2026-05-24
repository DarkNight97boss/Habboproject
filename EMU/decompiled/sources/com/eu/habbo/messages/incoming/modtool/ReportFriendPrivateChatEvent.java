package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.HelperRequestDisabledComposer;
import com.eu.habbo.threading.runnables.InsertModToolIssue;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ReportFriendPrivateChatEvent.class */
public class ReportFriendPrivateChatEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().getHabboStats().allowTalk()) {
            this.client.sendResponse(new HelperRequestDisabledComposer());
            return;
        }
        String string = this.packet.readString();
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        ArrayList<ModToolChatLog> arrayList = new ArrayList<>();
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue2);
        HabboInfo habboInfo = habbo != null ? habbo.getHabboInfo() : HabboManager.getOfflineHabboInfo(iIntValue2);
        if (habboInfo != null) {
            for (int i = 0; i < iIntValue3; i++) {
                arrayList.add(new ModToolChatLog(0, this.packet.readInt().intValue(), this.packet.readInt().intValue() == habboInfo.getId() ? habboInfo.getUsername() : this.client.getHabbo().getHabboInfo().getUsername(), this.packet.readString()));
            }
        }
        ModToolIssue modToolIssue = new ModToolIssue(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getUsername(), iIntValue2, habboInfo.getUsername(), 0, string, ModToolTicketType.IM);
        modToolIssue.category = iIntValue;
        modToolIssue.chatLogs = arrayList;
        new InsertModToolIssue(modToolIssue).run();
        Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue);
        Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue);
    }
}
