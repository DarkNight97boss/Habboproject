package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/DisconnectCommand.class */
public class DisconnectCommand extends Command {
    public DisconnectCommand() {
        super("cmd_disconnect", Emulator.getTexts().getValue("commands.keys.cmd_disconnect").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_disconnect.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr[1].toLowerCase().equals(gameClient.getHabbo().getHabboInfo().getUsername().toLowerCase())) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_disconnect.disconnect_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_disconnect.user_offline"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo.getHabboInfo().getRank().getId() > gameClient.getHabbo().getHabboInfo().getRank().getId()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_disconnect.higher_rank"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        habbo.getClient().getChannel().close();
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_disconnect.disconnected").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
