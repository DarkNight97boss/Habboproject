package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserIgnoredComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/MuteCommand.class */
public class MuteCommand extends Command {
    public MuteCommand() {
        super("cmd_mute", Emulator.getTexts().getValue("commands.keys.cmd_mute").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.not_specified"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo == gameClient.getHabbo()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        int iIntValue = Integer.MAX_VALUE;
        if (strArr.length == 3) {
            try {
                iIntValue = Integer.valueOf(strArr[2]).intValue();
                if (iIntValue <= 0) {
                    throw new Exception(Emulator.PREVIEW);
                }
            } catch (Exception e) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mute.time"), RoomChatMessageBubbles.ALERT);
                return true;
            }
        }
        habbo.mute(iIntValue, false);
        if (habbo.getHabboInfo().getCurrentRoom() != null) {
            habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserIgnoredComposer(habbo, 2).compose());
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_mute.muted").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
