package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRemoveComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUsersComposer;
import com.eu.habbo.threading.runnables.RoomUnitTeleport;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/InvisibleCommand.class */
public class InvisibleCommand extends Command {
    public InvisibleCommand() {
        super("cmd_invisible", Emulator.getTexts().getValue("commands.keys.cmd_invisible").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        RoomUnit roomUnit = gameClient.getHabbo().getRoomUnit();
        if (!roomUnit.isInvisible()) {
            roomUnit.setInvisible(true);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_invisible.updated"));
            gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserRemoveComposer(roomUnit).compose());
            return true;
        }
        RoomLayout layout = roomUnit.getRoom().getLayout();
        new RoomUnitTeleport(roomUnit, roomUnit.getRoom(), layout.getDoorTile().x, layout.getDoorTile().y, layout.getDoorTile().z, 0).run();
        roomUnit.setInvisible(false);
        roomUnit.setInRoom(true);
        roomUnit.getRoom().sendComposer(new RoomUsersComposer(gameClient.getHabbo()).compose());
        roomUnit.getRoom().sendComposer(new RoomUserStatusComposer(roomUnit).compose());
        WiredHandler.handle(WiredTriggerType.ENTER_ROOM, roomUnit, roomUnit.getRoom(), (Object[]) null);
        roomUnit.getRoom().habboEntered(gameClient.getHabbo());
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_invisible.updated.back"));
        return true;
    }
}
