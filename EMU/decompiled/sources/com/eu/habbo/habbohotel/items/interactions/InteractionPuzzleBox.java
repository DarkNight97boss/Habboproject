package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionPuzzleBox.class */
public class InteractionPuzzleBox extends HabboItem {
    public InteractionPuzzleBox(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionPuzzleBox(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        RoomTile tile = room.getLayout().getTile(getX(), getY());
        RoomUserRotation roomUserRotation = null;
        if (getX() == gameClient.getHabbo().getRoomUnit().getX()) {
            if (getY() == gameClient.getHabbo().getRoomUnit().getY() + 1) {
                roomUserRotation = RoomUserRotation.SOUTH;
            } else if (getY() == gameClient.getHabbo().getRoomUnit().getY() - 1) {
                roomUserRotation = RoomUserRotation.NORTH;
            }
        } else if (getY() == gameClient.getHabbo().getRoomUnit().getY()) {
            if (getX() == gameClient.getHabbo().getRoomUnit().getX() + 1) {
                roomUserRotation = RoomUserRotation.EAST;
            } else if (getX() == gameClient.getHabbo().getRoomUnit().getX() - 1) {
                roomUserRotation = RoomUserRotation.WEST;
            }
        }
        if (roomUserRotation == null) {
            RoomTile closestAdjacentTile = gameClient.getHabbo().getRoomUnit().getClosestAdjacentTile(getX(), getY(), false);
            if (closestAdjacentTile != null) {
                gameClient.getHabbo().getRoomUnit().setGoalLocation(closestAdjacentTile);
                return;
            }
            return;
        }
        super.onClick(gameClient, room, new Object[]{"TOGGLE_OVERRIDE"});
        RoomTile tileInFront = room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), roomUserRotation.getValue());
        if (tileInFront == null || tileInFront.getState() == RoomTileState.INVALID || room.hasHabbosAt(tileInFront.x, tileInFront.y) || !tile.equals(room.getLayout().getTileInFront(gameClient.getHabbo().getRoomUnit().getCurrentLocation(), roomUserRotation.getValue()))) {
            return;
        }
        if (room.getTopItemAt(tileInFront.x, tileInFront.y) == null || room.getTopItemAt(tileInFront.x, tileInFront.y).getBaseItem().allowStack()) {
            setZ(room.getStackHeight(tileInFront.x, tileInFront.y, false));
            needsUpdate(true);
            room.updateItem(this);
            room.scheduledComposers.add(new FloorItemOnRollerComposer(this, null, tileInFront, 0.0d, room).compose());
            room.scheduledTasks.add(() -> {
                gameClient.getHabbo().getRoomUnit().setGoalLocation(tile);
                room.scheduledTasks.add(() -> {
                    gameClient.getHabbo().getRoomUnit().setGoalLocation(tile);
                });
            });
            needsUpdate(true);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }
}
