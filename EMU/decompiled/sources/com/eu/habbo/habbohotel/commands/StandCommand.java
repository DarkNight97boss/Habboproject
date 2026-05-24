package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/StandCommand.class */
public class StandCommand extends Command {
    public StandCommand() {
        super(null, Emulator.getTexts().getValue("commands.keys.cmd_stand").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getRiding() != null) {
            return true;
        }
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().makeStand(gameClient.getHabbo());
        return true;
    }
}
