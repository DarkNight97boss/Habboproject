package com.eu.habbo.habbohotel.items.interactions.games.battlebanzai;

import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.math3.util.Pair;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/battlebanzai/InteractionBattleBanzaiTile.class */
public class InteractionBattleBanzaiTile extends HabboItem {
    public InteractionBattleBanzaiTile(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionBattleBanzaiTile(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Habbo habbo;
        BattleBanzaiGame battleBanzaiGame;
        super.onWalkOn(roomUnit, room, objArr);
        if (getExtradata().isEmpty()) {
            setExtradata("0");
        }
        int iIntValue = Integer.valueOf(getExtradata()).intValue();
        if (iIntValue % 3 == 2 || (habbo = room.getHabbo(roomUnit)) == null || isLocked() || habbo.getHabboInfo().getCurrentGame() == null || !habbo.getHabboInfo().getCurrentGame().equals(BattleBanzaiGame.class) || (battleBanzaiGame = (BattleBanzaiGame) room.getGame(BattleBanzaiGame.class)) == null || !battleBanzaiGame.state.equals(GameState.RUNNING)) {
            return;
        }
        battleBanzaiGame.markTile(habbo, this, iIntValue);
    }

    public boolean isLocked() {
        return !getExtradata().isEmpty() && Integer.valueOf(getExtradata()).intValue() % 3 == 2;
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

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        super.onPickUp(room);
        setExtradata("0");
        room.updateItem(this);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPlace(Room room) {
        super.onPlace(room);
        BattleBanzaiGame battleBanzaiGame = (BattleBanzaiGame) room.getGame(BattleBanzaiGame.class);
        if (battleBanzaiGame == null || battleBanzaiGame.getState() == GameState.IDLE) {
            return;
        }
        setExtradata("1");
    }
}
