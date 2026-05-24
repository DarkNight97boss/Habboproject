package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/MassCreditsCommand.class */
public class MassCreditsCommand extends Command {
    public MassCreditsCommand() {
        super("cmd_masscredits", Emulator.getTexts().getValue("commands.keys.cmd_masscredits").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_masscredits.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        try {
            int iIntValue = Integer.valueOf(strArr[1]).intValue();
            if (iIntValue == 0) {
                return true;
            }
            Iterator<Map.Entry<Integer, Habbo>> it = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet().iterator();
            while (it.hasNext()) {
                Habbo value = it.next().getValue();
                value.giveCredits(iIntValue);
                if (value.getHabboInfo().getCurrentRoom() != null) {
                    value.whisper(Emulator.getTexts().getValue("commands.generic.cmd_credits.received").replace("%amount%", iIntValue + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
                }
            }
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_masscredits.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
