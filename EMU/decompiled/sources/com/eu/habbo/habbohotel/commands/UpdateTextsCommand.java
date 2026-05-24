package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdateTextsCommand.class */
public class UpdateTextsCommand extends Command {
    public UpdateTextsCommand() {
        super("cmd_update_texts", Emulator.getTexts().getValue("commands.keys.cmd_update_texts").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        try {
            Emulator.getTexts().reload();
            Emulator.getGameEnvironment().getCommandHandler().reloadCommands();
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_texts"), RoomChatMessageBubbles.ALERT);
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_update_texts.failed"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
