package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/TrashCommand.class */
public class TrashCommand extends Command {
    public TrashCommand() {
        super("cmd_trash", Emulator.getTexts().getValue("commands.keys.cmd_trash").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        gameClient.getHabbo().whisper("Sorry. Lulz mode removed |");
        return false;
    }
}
