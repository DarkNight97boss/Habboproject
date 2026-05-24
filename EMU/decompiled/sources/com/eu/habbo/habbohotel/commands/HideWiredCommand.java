package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/HideWiredCommand.class */
public class HideWiredCommand extends Command {
    public HideWiredCommand() {
        super("cmd_hidewired", Emulator.getTexts().getValue("commands.keys.cmd_hidewired").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return true;
        }
        if (!currentRoom.isOwner(gameClient.getHabbo())) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.errors.cmd_hidewired.permission"));
            return true;
        }
        currentRoom.setHideWired(!currentRoom.isHideWired());
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_hidewired." + (currentRoom.isHideWired() ? "hidden" : "shown")));
        return true;
    }
}
