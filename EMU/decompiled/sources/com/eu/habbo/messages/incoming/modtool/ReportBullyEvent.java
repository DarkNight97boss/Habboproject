package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuardianTicket;
import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.BullyReportedMessageComposer;
import com.eu.habbo.messages.outgoing.modtool.HelperRequestDisabledComposer;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ReportBullyEvent.class */
public class ReportBullyEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room room;
        Habbo habbo;
        if (this.client.getHabbo().getHabboStats().allowTalk()) {
            this.client.sendResponse(new HelperRequestDisabledComposer());
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        if (iIntValue == this.client.getHabbo().getHabboInfo().getId() || (room = Emulator.getGameEnvironment().getRoomManager().getRoom(iIntValue2)) == null || (habbo = room.getHabbo(iIntValue)) == null) {
            return;
        }
        if (Emulator.getGameEnvironment().getGuideManager().getOpenReportedHabboTicket(habbo) != null) {
            this.client.sendResponse(new BullyReportedMessageComposer(3));
            return;
        }
        ArrayList<ModToolChatLog> roomChatlog = Emulator.getGameEnvironment().getModToolManager().getRoomChatlog(iIntValue2);
        if (roomChatlog.isEmpty()) {
            this.client.sendResponse(new BullyReportedMessageComposer(2));
        } else {
            Emulator.getGameEnvironment().getGuideManager().addGuardianTicket(new GuardianTicket(this.client.getHabbo(), habbo, roomChatlog));
            this.client.sendResponse(new BullyReportedMessageComposer(0));
        }
    }
}
