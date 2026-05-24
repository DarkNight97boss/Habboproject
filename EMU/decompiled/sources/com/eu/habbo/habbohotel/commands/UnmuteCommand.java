package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UnmuteCommand.class */
public class UnmuteCommand extends Command {
    public UnmuteCommand() {
        super("cmd_unmute", Emulator.getTexts().getValue("commands.keys.cmd_unmute").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_unmute.not_specified"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_unmute.not_found").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo.getHabboStats().allowTalk() && (habbo.getHabboInfo().getCurrentRoom() == null || !habbo.getHabboInfo().getCurrentRoom().isMuted(habbo))) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_unmute.not_muted").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (!habbo.getHabboStats().allowTalk()) {
            habbo.unMute();
        }
        if (habbo.getHabboInfo().getCurrentRoom() != null && habbo.getHabboInfo().getCurrentRoom().isMuted(habbo)) {
            habbo.getHabboInfo().getCurrentRoom().muteHabbo(habbo, 1);
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_unmute").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
