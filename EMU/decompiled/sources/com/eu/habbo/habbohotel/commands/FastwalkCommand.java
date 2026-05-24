package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/FastwalkCommand.class */
public class FastwalkCommand extends Command {
    public FastwalkCommand() {
        super("cmd_fastwalk", Emulator.getTexts().getValue("commands.keys.cmd_fastwalk").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return false;
        }
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() != null && gameClient.getHabbo().getHabboInfo().getRiding() != null) {
            return true;
        }
        Habbo habbo = gameClient.getHabbo();
        if (strArr.length >= 2) {
            habbo = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(strArr[1]);
            if (habbo == null) {
                return false;
            }
        }
        habbo.getRoomUnit().setFastWalk(!habbo.getRoomUnit().isFastWalk());
        return true;
    }
}
