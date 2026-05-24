package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/ControlCommand.class */
public class ControlCommand extends Command {
    public ControlCommand() {
        super("cmd_control", Emulator.getTexts().getValue("commands.keys.cmd_control").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return true;
        }
        if (strArr.length != 2) {
            Object obj = gameClient.getHabbo().getRoomUnit().getCacheable().get("control");
            if (obj == null) {
                return true;
            }
            gameClient.getHabbo().getRoomUnit().getCacheable().remove("control");
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_control.stopped").replace("%user%", ((Habbo) obj).getHabboInfo().getUsername()), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_control.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo == gameClient.getHabbo()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_control.not_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo2 = (Habbo) gameClient.getHabbo().getRoomUnit().getCacheable().remove("control");
        if (habbo2 != null) {
            habbo2.getRoomUnit().getCacheable().remove("controller");
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_control.stopped").replace("%user%", habbo2.getHabboInfo().getUsername()), RoomChatMessageBubbles.ALERT);
        }
        gameClient.getHabbo().getRoomUnit().getCacheable().put("control", habbo);
        habbo.getRoomUnit().getCacheable().put("controller", gameClient.getHabbo());
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_control.controlling").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
