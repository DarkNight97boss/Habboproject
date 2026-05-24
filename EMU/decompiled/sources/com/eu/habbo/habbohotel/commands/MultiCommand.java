package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.rooms.items.PostItStickyPoleOpenComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/MultiCommand.class */
public class MultiCommand extends Command {
    public MultiCommand() {
        super("cmd_multi", Emulator.getTexts().getValue("commands.keys.cmd_multi").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        gameClient.sendResponse(new PostItStickyPoleOpenComposer(null));
        return true;
    }
}
