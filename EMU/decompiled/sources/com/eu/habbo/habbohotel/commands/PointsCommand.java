package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/PointsCommand.class */
public class PointsCommand extends Command {
    public PointsCommand() {
        super("cmd_points", Emulator.getTexts().getValue("commands.keys.cmd_points").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 3) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.user_offline").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            int iIntValue = Emulator.getConfig().getInt("seasonal.primary.type");
            if (strArr.length == 4) {
                try {
                    iIntValue = Integer.valueOf(strArr[3]).intValue();
                } catch (Exception e) {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_type").replace("%types%", Emulator.getConfig().getValue("seasonal.types").replace(";", ", ")), RoomChatMessageBubbles.ALERT);
                    return true;
                }
            }
            try {
                int iIntValue2 = Integer.valueOf(strArr[2]).intValue();
                if (iIntValue2 != 0) {
                    habbo.givePoints(iIntValue, iIntValue2);
                    if (habbo.getHabboInfo().getCurrentRoom() != null) {
                        habbo.whisper(Emulator.getTexts().getValue("commands.generic.cmd_points.received").replace("%amount%", iIntValue2 + Emulator.PREVIEW).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + iIntValue)), RoomChatMessageBubbles.ALERT);
                    } else {
                        habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_points.received").replace("%amount%", iIntValue2 + Emulator.PREVIEW).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + iIntValue)));
                    }
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_points.send").replace("%amount%", iIntValue2 + Emulator.PREVIEW).replace("%user%", strArr[1]).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + iIntValue)), RoomChatMessageBubbles.ALERT);
                } else {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_amount"), RoomChatMessageBubbles.ALERT);
                }
                return true;
            } catch (Exception e2) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_amount"), RoomChatMessageBubbles.ALERT);
                return true;
            }
        } catch (NumberFormatException e3) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
