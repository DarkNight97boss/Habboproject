package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/TeleportCommand.class */
public class TeleportCommand extends Command {
    public TeleportCommand() {
        super("cmd_teleport", Emulator.getTexts().getValue("commands.keys.cmd_teleport").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getRiding() != null) {
            return true;
        }
        if (gameClient.getHabbo().getRoomUnit().cmdTeleport) {
            gameClient.getHabbo().getRoomUnit().cmdTeleport = false;
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_teleport.disabled"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.getHabbo().getRoomUnit().cmdTeleport = true;
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_teleport.enabled"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
