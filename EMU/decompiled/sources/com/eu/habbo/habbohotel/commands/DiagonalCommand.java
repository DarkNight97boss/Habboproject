package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/DiagonalCommand.class */
public class DiagonalCommand extends Command {
    public DiagonalCommand() {
        super("cmd_diagonal", Emulator.getTexts().getValue("commands.keys.cmd_diagonal").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null || !gameClient.getHabbo().getHabboInfo().getCurrentRoom().hasRights(gameClient.getHabbo())) {
            return false;
        }
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().moveDiagonally(!gameClient.getHabbo().getHabboInfo().getCurrentRoom().moveDiagonally());
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom().moveDiagonally()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_diagonal.enabled"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_diagonal.disabled"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
