package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UnbanCommand.class */
public class UnbanCommand extends Command {
    public UnbanCommand() {
        super("cmd_unban", Emulator.getTexts().getValue("commands.keys.cmd_unban").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_unban.not_specified"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (Emulator.getGameEnvironment().getModToolManager().unban(strArr[1])) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_unban.success").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_unban.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
