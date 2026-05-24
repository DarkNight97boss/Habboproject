package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTalkComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SayCommand.class */
public class SayCommand extends Command {
    public SayCommand() {
        super("cmd_say", Emulator.getTexts().getValue("commands.keys.cmd_say").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_say.forgot_username"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_say.user_not_found"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo.getHabboInfo().getCurrentRoom() == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_say.hotel_view").replace("%user%", strArr[1]), RoomChatMessageBubbles.ALERT);
            return true;
        }
        StringBuilder sb = new StringBuilder();
        if (strArr.length > 2) {
            for (int i = 2; i < strArr.length; i++) {
                sb.append(strArr[i]).append(" ");
            }
        }
        habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserTalkComposer(new RoomChatMessage(sb.toString(), habbo, RoomChatMessageBubbles.NORMAL)).compose());
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_say").replace("%user%", strArr[1]).replace("%message%", sb.toString()), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
