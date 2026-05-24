package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/MoonwalkCommand.class */
public class MoonwalkCommand extends Command {
    public MoonwalkCommand() {
        super("cmd_moonwalk", Emulator.getTexts().getValue("commands.keys.cmd_moonwalk").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null || !gameClient.getHabbo().getHabboStats().hasActiveClub()) {
            return false;
        }
        int i = 136;
        if (gameClient.getHabbo().getRoomUnit().getEffectId() == 136) {
            i = 0;
        }
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(gameClient.getHabbo(), i, -1);
        return true;
    }
}
