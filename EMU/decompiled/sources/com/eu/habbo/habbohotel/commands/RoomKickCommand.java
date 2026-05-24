package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomKickCommand.class */
public class RoomKickCommand extends Command {
    public RoomKickCommand() {
        super("cmd_kickall", Emulator.getTexts().getValue("commands.keys.cmd_kickall").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return true;
        }
        if (strArr.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < strArr.length; i++) {
                sb.append(strArr[i]).append(" ");
            }
            currentRoom.sendComposer(new GenericAlertComposer(((Object) sb) + "\r\n-" + gameClient.getHabbo().getHabboInfo().getUsername()).compose());
        }
        for (Habbo habbo : currentRoom.getHabbos()) {
            if (!habbo.hasPermission(Permission.ACC_UNKICKABLE) && !habbo.hasPermission(Permission.ACC_SUPPORTTOOL) && !currentRoom.isOwner(habbo)) {
                currentRoom.kickHabbo(habbo, true);
            }
        }
        return true;
    }
}
