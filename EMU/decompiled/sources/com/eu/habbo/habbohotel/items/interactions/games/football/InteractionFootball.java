package com.eu.habbo.habbohotel.items.interactions.games.football;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.football.FootballGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionPushable;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTeamItem;
import com.eu.habbo.habbohotel.items.interactions.games.football.goals.InteractionFootballGoal;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.ItemStateComposer;
import com.eu.habbo.util.pathfinding.Rotation;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/football/InteractionFootball.class */
public class InteractionFootball extends InteractionPushable {
    public InteractionFootball(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionFootball(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getWalkOnVelocity(RoomUnit roomUnit, Room room) {
        if (roomUnit.getPath().isEmpty() && roomUnit.tilesWalked() == 2 && getExtradata().equals("1")) {
            return 0;
        }
        return (roomUnit.getPath().size() == 0 && roomUnit.tilesWalked() == 1) ? 6 : 1;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getWalkOffVelocity(RoomUnit roomUnit, Room room) {
        return (roomUnit.getPath().size() == 0 && roomUnit.tilesWalked() == 0) ? 6 : 1;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getDragVelocity(RoomUnit roomUnit, Room room) {
        return (roomUnit.getPath().isEmpty() && roomUnit.tilesWalked() == 2) ? 0 : 1;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getTackleVelocity(RoomUnit roomUnit, Room room) {
        return 4;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getWalkOnDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getWalkOffDirection(RoomUnit roomUnit, Room room) {
        RoomTile roomTilePeek = roomUnit.getPath().peek();
        RoomTile tile = roomTilePeek != null ? room.getLayout().getTile(roomTilePeek.x, roomTilePeek.y) : roomUnit.getGoal();
        return RoomUserRotation.values()[((RoomUserRotation.values().length + Rotation.Calculate(roomUnit.getX(), roomUnit.getY(), tile.x, tile.y)) + 4) % 8];
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getDragDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public RoomUserRotation getTackleDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public int getNextRollDelay(int i, int i2) {
        return (i2 <= 4 || i > 4) ? 500 : 125;
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

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public boolean validMove(Room room, RoomTile roomTile, RoomTile roomTile2) {
        if (roomTile2 == null || roomTile2.state == RoomTileState.INVALID) {
            return false;
        }
        HabboItem topItemAt = room.getTopItemAt(roomTile2.x, roomTile2.y, this);
        if (topItemAt == null) {
            return true;
        }
        if (room.getItemsAt(roomTile2).stream().anyMatch(habboItem -> {
            return !habboItem.getBaseItem().allowStack();
        }) || BigDecimal.valueOf(topItemAt.getZ() + topItemAt.getBaseItem().getHeight()).subtract(BigDecimal.valueOf(getZ())).compareTo(new BigDecimal(1.65d)) > 0) {
            return false;
        }
        if (topItemAt instanceof InteractionFootballGoal) {
            int iCalculate = Rotation.Calculate(roomTile.x, roomTile.y, roomTile2.x, roomTile2.y);
            switch (topItemAt.getRotation()) {
                case 0:
                    return iCalculate > 2 && iCalculate < 6;
                case 2:
                    return iCalculate > 4;
                case 4:
                    return iCalculate > 6 || iCalculate < 2;
                case 6:
                    return iCalculate > 0 && iCalculate < 4;
            }
        }
        return topItemAt.getBaseItem().allowStack();
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
        FootballGame footballGame = (FootballGame) room.getGame(FootballGame.class);
        if (footballGame == null) {
            try {
                footballGame = (FootballGame) FootballGame.class.getDeclaredConstructor(Room.class).newInstance(room);
                room.addGame(footballGame);
            } catch (Exception e) {
                return;
            }
        }
        HabboItem topItemAt = room.getTopItemAt(roomTile.x, roomTile.y, this);
        HabboItem topItemAt2 = room.getTopItemAt(roomTile2.x, roomTile2.y, this);
        if (topItemAt2 != null && ((topItemAt == null || topItemAt.getId() != topItemAt2.getId()) && (topItemAt2 instanceof InteractionFootballGoal))) {
            footballGame.onScore(roomUnit, ((InteractionGameTeamItem) topItemAt2).teamColor);
        }
        setExtradata(Math.abs(i2 - (i3 + 1)) + Emulator.PREVIEW);
        room.sendComposer(new ItemStateComposer(this).compose());
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public void onBounce(Room room, RoomUserRotation roomUserRotation, RoomUserRotation roomUserRotation2, RoomUnit roomUnit) {
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public void onStop(Room room, RoomUnit roomUnit, int i, int i2) {
        setExtradata("0");
        room.sendComposer(new ItemStateComposer(this).compose());
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionPushable
    public boolean canStillMove(Room room, RoomTile roomTile, RoomTile roomTile2, RoomUserRotation roomUserRotation, RoomUnit roomUnit, int i, int i2, int i3) {
        HabboItem topItemAt = room.getTopItemAt(roomTile.x, roomTile.y, this);
        return (Emulator.getRandom().nextInt(10) < 3 || !room.hasHabbosAt(roomTile2.x, roomTile2.y)) && (topItemAt == null || !topItemAt.getBaseItem().getName().startsWith("fball_goal_") || i2 == 1);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        setExtradata("0");
    }
}
