package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/ReloadRoomCommand.class */
public class ReloadRoomCommand extends Command {
    public ReloadRoomCommand() {
        super("cmd_reload_room", Emulator.getTexts().getValue("commands.keys.cmd_reload_room").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Emulator.getThreading().run(() -> {
            Room currentRoom = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
            if (currentRoom != null) {
                ArrayList arrayList = new ArrayList(currentRoom.getHabbos());
                Emulator.getGameEnvironment().getRoomManager().unloadRoom(currentRoom);
                ServerMessage serverMessageCompose = new ForwardToRoomComposer(Emulator.getGameEnvironment().getRoomManager().loadRoom(currentRoom.getId()).getId()).compose();
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    ((Habbo) it.next()).getClient().sendResponse(serverMessageCompose);
                }
            }
        }, 100L);
        return true;
    }
}
