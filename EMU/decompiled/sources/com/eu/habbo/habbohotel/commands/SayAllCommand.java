package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SayAllCommand.class */
public class SayAllCommand extends Command {
    public SayAllCommand() {
        super("cmd_say_all", Emulator.getTexts().getValue("commands.keys.cmd_say_all").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_say_all.forgot_message"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < strArr.length; i++) {
            sb.append(strArr[i]).append(" ");
        }
        Iterator<Habbo> it = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbos().iterator();
        while (it.hasNext()) {
            it.next().talk(sb.toString());
        }
        return true;
    }
}
