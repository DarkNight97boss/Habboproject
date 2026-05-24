package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/LayCommand.class */
public class LayCommand extends Command {
    public LayCommand() {
        super(null, Emulator.getTexts().getValue("commands.keys.cmd_lay").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getRoomUnit() == null || !gameClient.getHabbo().getRoomUnit().canForcePosture()) {
            return true;
        }
        gameClient.getHabbo().getRoomUnit().cmdLay = true;
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().updateHabbo(gameClient.getHabbo());
        gameClient.getHabbo().getRoomUnit().cmdSit = true;
        gameClient.getHabbo().getRoomUnit().setBodyRotation(RoomUserRotation.values()[gameClient.getHabbo().getRoomUnit().getBodyRotation().getValue() - (gameClient.getHabbo().getRoomUnit().getBodyRotation().getValue() % 2)]);
        RoomTile currentLocation = gameClient.getHabbo().getRoomUnit().getCurrentLocation();
        if (currentLocation == null) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            RoomTile tileInFront = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTileInFront(currentLocation, gameClient.getHabbo().getRoomUnit().getBodyRotation().getValue(), i);
            if (tileInFront == null || !tileInFront.isWalkable()) {
                return false;
            }
        }
        gameClient.getHabbo().getRoomUnit().setStatus(RoomUnitStatus.LAY, "0.5");
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserStatusComposer(gameClient.getHabbo().getRoomUnit()).compose());
        return true;
    }
}
