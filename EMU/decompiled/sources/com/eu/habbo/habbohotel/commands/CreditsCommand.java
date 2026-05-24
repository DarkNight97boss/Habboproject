package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/CreditsCommand.class */
public class CreditsCommand extends Command {
    public CreditsCommand() {
        super("cmd_credits", Emulator.getTexts().getValue("commands.keys.cmd_credits").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 3) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_credits.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(strArr[1]);
        if (offlineHabboInfo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_credits.user_not_found").replace("%amount%", Integer.parseInt(strArr[2]) + Emulator.PREVIEW).replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(strArr[1]);
        try {
            int i = Integer.parseInt(strArr[2]);
            if (habbo == null) {
                Emulator.getGameEnvironment().getHabboManager().giveCredits(offlineHabboInfo.getId(), i);
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_credits.send").replace("%amount%", Integer.parseInt(strArr[2]) + Emulator.PREVIEW).replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
                return true;
            }
            if (i == 0) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_credits.invalid_amount"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            habbo.giveCredits(i);
            if (habbo.getHabboInfo().getCurrentRoom() != null) {
                habbo.whisper(Emulator.getTexts().getValue("commands.generic.cmd_credits.received").replace("%amount%", Integer.parseInt(strArr[2]) + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
            } else {
                habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_credits.received").replace("%amount%", Integer.parseInt(strArr[2]) + Emulator.PREVIEW));
            }
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_credits.send").replace("%amount%", Integer.parseInt(strArr[2]) + Emulator.PREVIEW).replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        } catch (NumberFormatException e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_credits.invalid_amount"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
