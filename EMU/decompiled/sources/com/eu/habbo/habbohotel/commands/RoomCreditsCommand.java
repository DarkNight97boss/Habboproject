package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomCreditsCommand.class */
public class RoomCreditsCommand extends Command {
    public RoomCreditsCommand() {
        super("cmd_roomcredits", Emulator.getTexts().getValue("commands.keys.cmd_roomcredits").split(";"));
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
            String strReplace = Emulator.getTexts().getValue("commands.generic.cmd_credits.received").replace("%amount%", iIntValue + Emulator.PREVIEW);
            for (Habbo habbo : gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbos()) {
                habbo.giveCredits(iIntValue);
                habbo.whisper(strReplace, RoomChatMessageBubbles.ALERT);
            }
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_masscredits.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
