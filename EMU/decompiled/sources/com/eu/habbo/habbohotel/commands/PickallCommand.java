package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/PickallCommand.class */
public class PickallCommand extends Command {
    public PickallCommand() {
        super("cmd_pickall", Emulator.getTexts().getValue("commands.keys.cmd_pickall").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return true;
        }
        if (currentRoom.isOwner(gameClient.getHabbo())) {
            currentRoom.ejectAll();
            return true;
        }
        currentRoom.ejectUserFurni(gameClient.getHabbo().getHabboInfo().getId());
        return true;
    }
}
