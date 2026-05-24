package com.eu.habbo.habbohotel.items.interactions.games.freeze;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.freeze.FreezeGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.math3.util.Pair;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/freeze/InteractionFreezeTile.class */
public class InteractionFreezeTile extends HabboItem {
    public InteractionFreezeTile(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionFreezeTile(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        FreezeGame freezeGame;
        if (gameClient != null && gameClient.getHabbo().getRoomUnit().getCurrentLocation().x == getX() && gameClient.getHabbo().getRoomUnit().getCurrentLocation().y == getY() && (freezeGame = (FreezeGame) room.getGame(FreezeGame.class)) != null) {
            freezeGame.throwBall(gameClient.getHabbo(), this);
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canStackAt(Room room, List<Pair<RoomTile, THashSet<HabboItem>>> list) {
        for (Pair<RoomTile, THashSet<HabboItem>> pair : list) {
            if (pair.getValue() != null && !((THashSet) pair.getValue()).isEmpty()) {
                return false;
            }
        }
        return super.canStackAt(room, list);
    }
}
