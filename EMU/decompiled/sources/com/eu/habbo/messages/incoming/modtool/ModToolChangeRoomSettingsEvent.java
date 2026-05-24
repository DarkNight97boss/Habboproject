package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/modtool/ModToolChangeRoomSettingsEvent.class */
public class ModToolChangeRoomSettingsEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_SUPPORTTOOL)) {
            ScripterManager.scripterDetected(this.client, Emulator.getTexts().getValue("scripter.warning.modtools.roomsettings").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()));
            return;
        }
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.packet.readInt().intValue());
        if (room != null) {
            Emulator.getGameEnvironment().getModToolManager().roomAction(room, this.client.getHabbo(), this.packet.readInt().intValue() == 1, this.packet.readInt().intValue() == 1, this.packet.readInt().intValue() == 1);
        }
    }
}
