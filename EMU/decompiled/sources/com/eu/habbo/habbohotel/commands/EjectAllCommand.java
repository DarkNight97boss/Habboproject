package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomRightLevels;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/EjectAllCommand.class */
public class EjectAllCommand extends Command {
    public EjectAllCommand() {
        super("cmd_ejectall", Emulator.getTexts().getValue("commands.keys.cmd_ejectall").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return true;
        }
        if (!currentRoom.isOwner(gameClient.getHabbo()) && (!currentRoom.hasGuild() || !currentRoom.getGuildRightLevel(gameClient.getHabbo()).equals(RoomRightLevels.GUILD_ADMIN))) {
            return true;
        }
        currentRoom.ejectAll(gameClient.getHabbo());
        return true;
    }
}
