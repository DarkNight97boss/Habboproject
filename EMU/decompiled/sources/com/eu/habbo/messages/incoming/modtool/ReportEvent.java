package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuardianTicket;
import com.eu.habbo.habbohotel.modtool.CfhActionType;
import com.eu.habbo.habbohotel.modtool.CfhTopic;
import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketState;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.BullyReportedMessageComposer;
import com.eu.habbo.messages.outgoing.modtool.HelperRequestDisabledComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueHandledComposer;
import com.eu.habbo.messages.outgoing.modtool.ModToolReportReceivedAlertComposer;
import com.eu.habbo.messages.outgoing.modtool.ReportRoomFormComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserIgnoredComposer;
import com.eu.habbo.threading.runnables.InsertModToolIssue;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ReportEvent.class */
public class ReportEvent extends MessageHandler {
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
        this.packet.readInt().intValue();
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(iIntValue3);
        List<ModToolIssue> listOpenTicketsForHabbo = Emulator.getGameEnvironment().getModToolManager().openTicketsForHabbo(this.client.getHabbo());
        if (!listOpenTicketsForHabbo.isEmpty()) {
            this.client.sendResponse(new ReportRoomFormComposer(listOpenTicketsForHabbo));
            return;
        }
        CfhTopic cfhTopic = Emulator.getGameEnvironment().getModToolManager().getCfhTopic(iIntValue);
        if (iIntValue2 == -1) {
            ModToolIssue modToolIssue = new ModToolIssue(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getUsername(), room != null ? room.getOwnerId() : 0, room != null ? room.getOwnerName() : Emulator.PREVIEW, iIntValue3, string, ModToolTicketType.ROOM);
            modToolIssue.category = iIntValue;
            new InsertModToolIssue(modToolIssue).run();
            this.client.sendResponse(new ModToolReportReceivedAlertComposer(0, string));
            Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue);
            Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue);
            if (cfhTopic == null || cfhTopic.action == CfhActionType.MODS) {
                return;
            }
            Emulator.getThreading().run(() -> {
                Habbo habbo;
                if (modToolIssue.state == ModToolTicketState.OPEN) {
                    if (cfhTopic.action == CfhActionType.AUTO_IGNORE && this.client.getHabbo().getHabboStats().ignoreUser(this.client, modToolIssue.reportedId) && (habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(modToolIssue.reportedId)) != null) {
                        this.client.sendResponse(new RoomUserIgnoredComposer(habbo, 1));
                    }
                    this.client.sendResponse(new ModToolIssueHandledComposer(cfhTopic.reply).compose());
                    Emulator.getGameEnvironment().getModToolManager().closeTicketAsHandled(modToolIssue, null);
                }
            }, 30000L);
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(iIntValue2);
        if (habbo != null) {
            if (cfhTopic != null && cfhTopic.action == CfhActionType.GUARDIANS && Emulator.getGameEnvironment().getGuideManager().activeGuardians()) {
                if (Emulator.getGameEnvironment().getGuideManager().getOpenReportedHabboTicket(habbo) != null) {
                    this.client.sendResponse(new BullyReportedMessageComposer(3));
                    return;
                }
                ArrayList<ModToolChatLog> roomChatlog = Emulator.getGameEnvironment().getModToolManager().getRoomChatlog(iIntValue3);
                if (roomChatlog.isEmpty()) {
                    this.client.sendResponse(new BullyReportedMessageComposer(2));
                    return;
                } else {
                    Emulator.getGameEnvironment().getGuideManager().addGuardianTicket(new GuardianTicket(this.client.getHabbo(), habbo, roomChatlog));
                    this.client.sendResponse(new BullyReportedMessageComposer(0));
                    return;
                }
            }
            ModToolIssue modToolIssue2 = new ModToolIssue(this.client.getHabbo().getHabboInfo().getId(), this.client.getHabbo().getHabboInfo().getUsername(), habbo.getHabboInfo().getId(), habbo.getHabboInfo().getUsername(), iIntValue3, string, ModToolTicketType.NORMAL);
            modToolIssue2.category = iIntValue;
            new InsertModToolIssue(modToolIssue2).run();
            Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue2);
            Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue2);
            this.client.sendResponse(new ModToolReportReceivedAlertComposer(0, cfhTopic.reply));
            if (cfhTopic == null || cfhTopic.action == CfhActionType.MODS) {
                return;
            }
            Emulator.getThreading().run(() -> {
                if (modToolIssue2.state == ModToolTicketState.OPEN) {
                    if (cfhTopic.action == CfhActionType.AUTO_IGNORE && this.client.getHabbo().getHabboStats().ignoreUser(this.client, habbo.getHabboInfo().getId())) {
                        this.client.sendResponse(new RoomUserIgnoredComposer(habbo, 1));
                    }
                    this.client.sendResponse(new ModToolIssueHandledComposer(cfhTopic.reply).compose());
                    Emulator.getGameEnvironment().getModToolManager().closeTicketAsHandled(modToolIssue2, null);
                }
            }, 30000L);
        }
    }
}
