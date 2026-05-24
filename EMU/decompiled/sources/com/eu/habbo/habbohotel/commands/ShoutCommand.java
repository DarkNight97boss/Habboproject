package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserShoutComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/ShoutCommand.class */
public class ShoutCommand extends Command {
    private static String idea = "Kudo's To Droppy for this idea!";

    public ShoutCommand() {
        super("cmd_shout", Emulator.getTexts().getValue("commands.keys.cmd_shout").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_shout.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_shout.user_not_found"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo.getHabboInfo().getCurrentRoom() == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_shout.hotel_view").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        StringBuilder sb = new StringBuilder();
        if (strArr.length > 2) {
            for (int i = 2; i < strArr.length; i++) {
                sb.append(strArr[i]).append(" ");
            }
        }
        habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserShoutComposer(new RoomChatMessage(sb.toString(), habbo, RoomChatMessageBubbles.NORMAL)).compose());
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_shout").replace("%user%", strArr[1]).replace("%message%", sb.toString()), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
