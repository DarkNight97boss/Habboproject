package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdateBotsCommand.class */
public class UpdateBotsCommand extends Command {
    public UpdateBotsCommand() {
        super("cmd_update_bots", Emulator.getTexts().getValue("commands.keys.cmd_update_bots").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        return Emulator.getGameEnvironment().getBotManager().reload();
    }
}
