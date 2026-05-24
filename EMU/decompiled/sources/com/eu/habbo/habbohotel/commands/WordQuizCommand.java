package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Arrays;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/WordQuizCommand.class */
public class WordQuizCommand extends Command {
    public WordQuizCommand() {
        super("cmd_word_quiz", Emulator.getTexts().getValue("commands.keys.cmd_word_quiz").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom().hasActiveWordQuiz()) {
            return true;
        }
        if (strArr.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.description.cmd_word_quiz"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        StringBuilder sb = new StringBuilder();
        int i = 60;
        try {
            i = Integer.parseInt(strArr[strArr.length - 1]);
            strArr = (String[]) Arrays.copyOf(strArr, strArr.length - 1);
        } catch (Exception e) {
        }
        for (int i2 = 1; i2 < strArr.length; i2++) {
            sb.append(" ").append(strArr[i2]);
        }
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().startWordQuiz(sb.toString(), i * Outgoing.CraftableProductsComposer);
        return true;
    }
}
