package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/ChangeNameCommand.class */
public class ChangeNameCommand extends Command {
    public ChangeNameCommand() {
        super("cmd_changename", Emulator.getTexts().getValue("commands.keys.cmd_changename").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        gameClient.getHabbo().getHabboStats().allowNameChange = !gameClient.getHabbo().getHabboStats().allowNameChange;
        gameClient.sendResponse(new UserDataComposer(gameClient.getHabbo()));
        return true;
    }
}
