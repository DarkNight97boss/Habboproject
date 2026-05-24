package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UnloadRoomCommand.class */
public class UnloadRoomCommand extends Command {
    public UnloadRoomCommand() {
        super("cmd_unload", Emulator.getTexts().getValue("commands.keys.cmd_unload").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom().getOwnerId() != gameClient.getHabbo().getHabboInfo().getId() && gameClient.getHabbo().getHabboInfo().getRank().getId() <= 4) {
            return false;
        }
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().dispose();
        return true;
    }
}
