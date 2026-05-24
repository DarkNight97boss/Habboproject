package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/FreezeCommand.class */
public class FreezeCommand extends Command {
    public FreezeCommand() {
        super("cmd_freeze", Emulator.getTexts().getValue("commands.keys.cmd_freeze").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_freeze.not_found").replace("%user%", Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_freeze.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo.getRoomUnit().canWalk()) {
            habbo.getRoomUnit().setCanWalk(false);
            habbo.whisper(Emulator.getTexts().getValue("commands.succes.cmd_freeze.frozen"), RoomChatMessageBubbles.ALERT);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_freeze.user_frozen").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        habbo.getRoomUnit().setCanWalk(true);
        habbo.whisper(Emulator.getTexts().getValue("commands.succes.cmd_freeze.unfrozen"), RoomChatMessageBubbles.ALERT);
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_freeze.user_unfrozen").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
