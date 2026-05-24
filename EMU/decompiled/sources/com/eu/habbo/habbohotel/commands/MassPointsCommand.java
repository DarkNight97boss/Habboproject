package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/MassPointsCommand.class */
public class MassPointsCommand extends Command {
    public MassPointsCommand() {
        super("cmd_masspoints", Emulator.getTexts().getValue("commands.keys.cmd_masspoints").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        String str;
        int iIntValue = Emulator.getConfig().getInt("seasonal.primary.type");
        if (strArr.length == 3) {
            str = strArr[1];
            try {
                iIntValue = Integer.valueOf(strArr[2]).intValue();
            } catch (Exception e) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_masspoints.invalid_type").replace("%types%", Emulator.getConfig().getValue("seasonal.types").replace(";", ", ")), RoomChatMessageBubbles.ALERT);
                return true;
            }
        } else {
            if (strArr.length != 2) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_masspoints.invalid_amount"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            str = strArr[1];
        }
        boolean z = false;
        String[] strArrSplit = Emulator.getConfig().getValue("seasonal.types").split(";");
        int length = strArrSplit.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            if (strArrSplit[i].equalsIgnoreCase(iIntValue + Emulator.PREVIEW)) {
                z = true;
                break;
            }
            i++;
        }
        if (!z) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_masspoints.invalid_type").replace("%types%", Emulator.getConfig().getValue("seasonal.types").replace(";", ", ")), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            int iIntValue2 = Integer.valueOf(str).intValue();
            if (iIntValue2 == 0) {
                return true;
            }
            String strReplace = Emulator.getTexts().getValue("commands.generic.cmd_points.received").replace("%amount%", iIntValue2 + Emulator.PREVIEW).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + iIntValue));
            Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
            while (it.hasNext()) {
                Habbo value = it.next().getValue();
                value.givePoints(iIntValue, iIntValue2);
                if (value.getHabboInfo().getCurrentRoom() != null) {
                    value.whisper(strReplace, RoomChatMessageBubbles.ALERT);
                } else {
                    value.alert(strReplace);
                }
            }
            return true;
        } catch (Exception e2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_masspoints.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
