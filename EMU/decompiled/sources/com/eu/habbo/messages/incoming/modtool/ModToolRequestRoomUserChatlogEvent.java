package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolRoomChatlogComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolRequestRoomUserChatlogEvent.class */
public class ModToolRequestRoomUserChatlogEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom;
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.chatlog").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
        } else {
            if (Emulator.getGameEnvironment().getHabboManager().getHabbo(this.packet.readInt().intValue()) == null || (currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom()) == null) {
                return;
            }
            this.client.sendResponse(new ModToolRoomChatlogComposer(currentRoom, Emulator.getGameEnvironment().getModToolManager().getRoomChatlog(currentRoom.getId())));
        }
    }
}
