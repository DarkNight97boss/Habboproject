package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SetPollCommand.class */
public class SetPollCommand extends Command {
    public SetPollCommand() {
        super("cmd_set_poll", Emulator.getTexts().getValue("commands.keys.cmd_set_poll").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_set_poll.missing_arg"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return true;
        }
        int iIntValue = -1;
        try {
            iIntValue = Integer.valueOf(strArr[1]).intValue();
        } catch (Exception e) {
        }
        if (iIntValue < 0) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_set_poll.invalid_number"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (Emulator.getGameEnvironment().getPollManager().getPoll(iIntValue) == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_set_poll.not_found"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().setPollId(iIntValue);
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_set_poll"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
