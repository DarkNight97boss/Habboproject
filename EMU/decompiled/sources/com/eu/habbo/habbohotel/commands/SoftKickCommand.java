package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SoftKickCommand.class */
public class SoftKickCommand extends Command {
    public SoftKickCommand() {
        super("cmd_softkick", Emulator.getTexts().getValue("commands.keys.cmd_softkick").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 2) {
            return true;
        }
        String str = strArr[1];
        Habbo habbo = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(str);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.keys.cmd_softkick_error").replace("%user%", str), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo == gameClient.getHabbo()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.keys.cmd_softkick_error_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || habbo.hasPermission(Permission.ACC_UNKICKABLE) || habbo.hasPermission(Permission.ACC_SUPPORTTOOL) || currentRoom.isOwner(habbo)) {
            return true;
        }
        currentRoom.kickHabbo(habbo, false);
        return true;
    }
}
