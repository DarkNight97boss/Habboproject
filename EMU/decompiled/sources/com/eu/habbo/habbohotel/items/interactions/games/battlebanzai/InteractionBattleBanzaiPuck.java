package com.eu.habbo.habbohotel.items.interactions.games.battlebanzai;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionPushable;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/battlebanzai/InteractionBattleBanzaiPuck.class */
public class InteractionBattleBanzaiPuck extends InteractionPushable {
    public InteractionBattleBanzaiPuck(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionBattleBanzaiPuck(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getWalkOnVelocity(RoomUnit roomUnit, Room room) {
        return 6;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getWalkOnDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getWalkOffVelocity(RoomUnit roomUnit, Room room) {
        return 0;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getWalkOffDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getDragVelocity(RoomUnit roomUnit, Room room) {
        return 1;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getDragDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getTackleVelocity(RoomUnit roomUnit, Room room) {
        return 6;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getTackleDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getNextRollDelay(int i, int i2) {
        if (i2 == 1) {
            return 500;
        }
        int i3 = (2500 / 2500) - 1;
        return (100 * ((i3 * i3 * i3 * i3 * i3) + 1)) + (i * 100);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getBounceDirection(Room room, RoomUserRotation roomUserRotation) {
        switch (roomUserRotation) {
            case NORTH:
            default:
                return RoomUserRotation.SOUTH;
            case NORTH_EAST:
                return validMove(room, room.getLayout().getTile(getX(), getY()), room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), RoomUserRotation.NORTH_WEST.getValue())) ? RoomUserRotation.NORTH_WEST : validMove(room, room.getLayout().getTile(getX(), getY()), room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), RoomUserRotation.SOUTH_EAST.getValue())) ? RoomUserRotation.SOUTH_EAST : RoomUserRotation.SOUTH_WEST;
            case EAST:
                return RoomUserRotation.WEST;
            case SOUTH_EAST:
                return validMove(room, room.getLayout().getTile(getX(), getY()), room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), RoomUserRotation.SOUTH_WEST.getValue())) ? RoomUserRotation.SOUTH_WEST : validMove(room, room.getLayout().getTile(getX(), getY()), room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), RoomUserRotation.NORTH_EAST.getValue())) ? RoomUserRotation.NORTH_EAST : RoomUserRotation.NORTH_WEST;
            case SOUTH:
                return RoomUserRotation.NORTH;
            case SOUTH_WEST:
                return validMove(room, room.getLayout().getTile(getX(), getY()), room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), RoomUserRotation.SOUTH_EAST.getValue())) ? RoomUserRotation.SOUTH_EAST : validMove(room, room.getLayout().getTile(getX(), getY()), room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), RoomUserRotation.NORTH_WEST.getValue())) ? RoomUserRotation.NORTH_WEST : RoomUserRotation.NORTH_EAST;
            case WEST:
                return RoomUserRotation.EAST;
            case NORTH_WEST:
                return validMove(room, room.getLayout().getTile(getX(), getY()), room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), RoomUserRotation.NORTH_EAST.getValue())) ? RoomUserRotation.NORTH_EAST : validMove(room, room.getLayout().getTile(getX(), getY()), room.getLayout().getTileInFront(room.getLayout().getTile(getX(), getY()), RoomUserRotation.SOUTH_WEST.getValue())) ? RoomUserRotation.SOUTH_WEST : RoomUserRotation.SOUTH_EAST;
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable, com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public boolean validMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        if (roomTile2 == null) {
            return false;
        }
        HabboItem topItemAt = room.getTopItemAt(roomTile2.x, roomTile2.y, this);
        return room.getLayout().tileWalkable(roomTile2.x, roomTile2.y) && (topItemAt == null || !(!topItemAt.getBaseItem().allowStack() || topItemAt.getBaseItem().allowSit() || topItemAt.getBaseItem().allowLay()));
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public void onDrag(Room room, RoomUnit roomUnit, int i, RoomUserRotation roomUserRotation) {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public void onKick(Room room, RoomUnit roomUnit, int i, RoomUserRotation roomUserRotation) {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public void onTackle(Room room, RoomUnit roomUnit, int i, RoomUserRotation roomUserRotation) {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public void onMove(Room room, RoomTile roomTile, RoomTile roomTile2, RoomUserRotation roomUserRotation, RoomUnit roomUnit, int i, int i2, int i3) {
        BattleBanzaiGame battleBanzaiGame;
        GameTeam teamForHabbo;
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null || (battleBanzaiGame = (BattleBanzaiGame) room.getGame(BattleBanzaiGame.class)) == null || (teamForHabbo = battleBanzaiGame.getTeamForHabbo(habbo)) == null) {
            return;
        }
        try {
            room.getTopItemAt(roomTile2.x, roomTile2.y).onWalkOn(roomUnit, room, null);
            setExtradata(teamForHabbo.teamColor.type + Emulator.PREVIEW);
            room.updateItemState(this);
        } catch (Exception e) {
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public void onBounce(Room room, RoomUserRotation roomUserRotation, RoomUserRotation roomUserRotation2, RoomUnit roomUnit) {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public void onStop(Room room, RoomUnit roomUnit, int i, int i2) {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public boolean canStillMove(Room room, RoomTile roomTile, RoomTile roomTile2, RoomUserRotation roomUserRotation, RoomUnit roomUnit, int i, int i2, int i3) {
        return roomTile2.state == RoomTileState.OPEN && roomTile2.isWalkable();
    }
}
