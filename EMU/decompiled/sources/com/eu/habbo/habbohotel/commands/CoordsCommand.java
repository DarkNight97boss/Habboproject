package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/CoordsCommand.class */
public class CoordsCommand extends Command {
    public CoordsCommand() {
        super("cmd_coords", Emulator.getTexts().getValue("commands.keys.cmd_coords").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getRoomUnit() == null || gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return false;
        }
        if (strArr.length == 1) {
            gameClient.getHabbo().alert(Emulator.getTexts().getValue("commands.generic.cmd_coords.title") + "\r\nx: " + ((int) gameClient.getHabbo().getRoomUnit().getX()) + "\ry: " + ((int) gameClient.getHabbo().getRoomUnit().getY()) + "\rz: " + (gameClient.getHabbo().getRoomUnit().hasStatus(RoomUnitStatus.SIT) ? gameClient.getHabbo().getRoomUnit().getStatus(RoomUnitStatus.SIT) : Double.valueOf(gameClient.getHabbo().getRoomUnit().getZ())) + "\r" + Emulator.getTexts().getValue("generic.rotation.head") + ": " + gameClient.getHabbo().getRoomUnit().getHeadRotation() + "-" + gameClient.getHabbo().getRoomUnit().getHeadRotation().getValue() + "\r" + Emulator.getTexts().getValue("generic.rotation.body") + ": " + gameClient.getHabbo().getRoomUnit().getBodyRotation() + "-" + gameClient.getHabbo().getRoomUnit().getBodyRotation().getValue() + "\r" + Emulator.getTexts().getValue("generic.sitting") + ": " + (gameClient.getHabbo().getRoomUnit().hasStatus(RoomUnitStatus.SIT) ? Emulator.getTexts().getValue("generic.yes") : Emulator.getTexts().getValue("generic.no")) + "\rTile State: " + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTile(gameClient.getHabbo().getRoomUnit().getX(), gameClient.getHabbo().getRoomUnit().getY()).state.name() + "\rTile Walkable: " + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTile(gameClient.getHabbo().getRoomUnit().getX(), gameClient.getHabbo().getRoomUnit().getY()).isWalkable() + "\rTile relative height: " + ((int) gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTile(gameClient.getHabbo().getRoomUnit().getX(), gameClient.getHabbo().getRoomUnit().getY()).relativeHeight()) + "\rTile stack height: " + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTile(gameClient.getHabbo().getRoomUnit().getX(), gameClient.getHabbo().getRoomUnit().getY()).getStackHeight());
            return true;
        }
        RoomTile tile = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTile(Short.valueOf(strArr[1]).shortValue(), Short.valueOf(strArr[2]).shortValue());
        if (tile != null) {
            gameClient.getHabbo().alert(Emulator.getTexts().getValue("commands.generic.cmd_coords.title") + "\r\nx: " + ((int) tile.x) + "\ry: " + ((int) tile.y) + "\rz: " + ((int) tile.z) + "\rTile State: " + tile.state.name() + "\rTile Relative Height: " + ((int) tile.relativeHeight()) + "\rTile Stack Height: " + tile.getStackHeight() + "\rTile Walkable: " + (tile.isWalkable() ? "Yes" : "No") + "\r");
            return true;
        }
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("generic.tile.not.exists"));
        return true;
    }
}
