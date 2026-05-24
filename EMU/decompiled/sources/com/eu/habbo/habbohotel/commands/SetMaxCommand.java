package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SetMaxCommand.class */
public class SetMaxCommand extends Command {
    public SetMaxCommand() {
        super("cmd_setmax", Emulator.getTexts().getValue("commands.keys.cmd_setmax").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_setmax.forgot_number"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            int iIntValue = Integer.valueOf(strArr[1]).intValue();
            if (iIntValue <= 0 || iIntValue >= 9999) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_setmax.invalid_number"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            gameClient.getHabbo().getHabboInfo().getCurrentRoom().setUsersMax(iIntValue);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.success.cmd_setmax").replace("%value%", iIntValue + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
