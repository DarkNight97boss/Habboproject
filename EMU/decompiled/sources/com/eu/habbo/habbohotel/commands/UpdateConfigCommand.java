package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdateConfigCommand.class */
public class UpdateConfigCommand extends Command {
    public UpdateConfigCommand() {
        super("cmd_update_config", Emulator.getTexts().getValue("commands.keys.cmd_update_config").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Emulator.getConfig().reload();
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_config"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
