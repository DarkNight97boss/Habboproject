package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/HabnamCommand.class */
public class HabnamCommand extends Command {
    public HabnamCommand() {
        super(null, new String[]{"habnam", "gangnam"});
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (!gameClient.getHabbo().getHabboStats().hasActiveClub() || gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return false;
        }
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(gameClient.getHabbo(), 140, 30);
        return true;
    }
}
