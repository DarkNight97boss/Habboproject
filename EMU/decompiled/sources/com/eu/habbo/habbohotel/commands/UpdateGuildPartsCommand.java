package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdateGuildPartsCommand.class */
public class UpdateGuildPartsCommand extends Command {
    public UpdateGuildPartsCommand() {
        super("cmd_update_guildparts", Emulator.getTexts().getValue("commands.keys.cmd_update_guildparts").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Emulator.getGameEnvironment().getGuildManager().loadGuildParts();
        Emulator.getBadgeImager().reload();
        return true;
    }
}
