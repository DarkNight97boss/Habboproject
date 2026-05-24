package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/AlertCommand.class */
public class AlertCommand extends Command {
    public AlertCommand() {
        super("cmd_alert", Emulator.getTexts().getValue("commands.keys.cmd_alert").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_alert.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length < 3) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_alert.forgot_message"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        String str = strArr[1];
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < strArr.length; i++) {
            sb.append(strArr[i]).append(" ");
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(str);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_alert.user_offline").replace("%user%", str), RoomChatMessageBubbles.ALERT);
            return true;
        }
        habbo.alert(((Object) sb) + "\r\n    -" + gameClient.getHabbo().getHabboInfo().getUsername());
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_alert.message_send").replace("%user%", str), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
