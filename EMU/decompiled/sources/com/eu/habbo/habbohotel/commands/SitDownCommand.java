package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/SitDownCommand.class */
public class SitDownCommand extends Command {
    public SitDownCommand() {
        super("cmd_sitdown", Emulator.getTexts().getValue("commands.keys.cmd_sitdown").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        for (Habbo habbo : gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbos()) {
            if (habbo.getRoomUnit().isWalking()) {
                habbo.getRoomUnit().stopWalking();
            } else if (habbo.getRoomUnit().hasStatus(RoomUnitStatus.SIT)) {
            }
            gameClient.getHabbo().getHabboInfo().getCurrentRoom().makeSit(habbo);
        }
        return true;
    }
}
