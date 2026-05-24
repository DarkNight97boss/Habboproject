package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/PixelCommand.class */
public class PixelCommand extends Command {
    public PixelCommand() {
        super("cmd_duckets", Emulator.getTexts().getValue("commands.keys.cmd_duckets").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 3) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_duckets.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_duckets.user_offline").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            if (Integer.parseInt(strArr[2]) != 0) {
                habbo.givePixels(Integer.parseInt(strArr[2]));
                if (habbo.getHabboInfo().getCurrentRoom() != null) {
                    habbo.whisper(Emulator.getTexts().getValue("commands.generic.cmd_duckets.received").replace("%amount%", Integer.valueOf(strArr[2]) + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
                } else {
                    habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_duckets.received").replace("%amount%", Integer.valueOf(strArr[2]) + Emulator.PREVIEW));
                }
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_duckets.send").replace("%amount%", Integer.valueOf(strArr[2]) + Emulator.PREVIEW).replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            } else {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_duckets.invalid_amount"), RoomChatMessageBubbles.ALERT);
            }
            return true;
        } catch (NumberFormatException e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_duckets.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
