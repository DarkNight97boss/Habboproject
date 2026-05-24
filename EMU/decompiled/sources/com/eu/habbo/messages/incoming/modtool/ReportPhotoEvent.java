package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionExternalImage;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolReportReceivedAlertComposer;
import com.eu.habbo.threading.runnables.InsertModToolIssue;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ReportPhotoEvent.class */
public class ReportPhotoEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room room;
        HabboItem habboItem;
        HabboInfo habboInfo;
        boolean z = this.packet.readShort() != 0;
        this.packet.getBuffer().resetReaderIndex();
        if (z) {
            this.packet.readString();
        }
        int iIntValue = this.packet.readInt().intValue();
        this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        if (Emulator.getGameEnvironment().getModToolManager().getCfhTopic(iIntValue2) == null || (room = Emulator.getGameEnvironment().getRoomManager().getRoom(iIntValue)) == null || (habboItem = room.getHabboItem(iIntValue3)) == null || !(habboItem instanceof InteractionExternalImage) || (habboInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(habboItem.getUserId())) == null) {
            return;
        }
        ModToolIssue modToolIssue = new ModToolIssue(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getUsername(), habboInfo.getId(), habboInfo.getUsername(), iIntValue, Emulator.PREVIEW, ModToolTicketType.PHOTO);
        modToolIssue.photoItem = habboItem;
        new InsertModToolIssue(modToolIssue).run();
        this.client.sendResponse(new ModToolReportReceivedAlertComposer(0, Emulator.PREVIEW));
        Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue);
        Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue);
    }
}
